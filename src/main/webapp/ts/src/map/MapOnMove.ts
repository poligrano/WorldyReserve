import {containsExtent, Extent} from "ol/extent";
import {Feature, Map} from "ol";
import {Point} from "ol/geom";
import {fromLonLat, transformExtent} from "ol/proj";
import VectorSource from "ol/source/Vector";
import {Utils} from "./Utils";
import queryOverpass = Utils.queryOverpass;

function bboxBuffer(bbox: Extent, percentage: number): Extent
{
    const w: number = (bbox[2] - bbox[0]) * percentage / 100;
    const h: number = (bbox[3] - bbox[1]) * percentage / 100;
    return [bbox[0] - w, bbox[1] - h, bbox[2] + w, bbox[3] + h];
}

function addElementsToSource(source: VectorSource, elements: Array<any>): void
{
    console.log(`Parsing Overpass result: ${elements}`);
    source.addFeatures(elements.map((e: any): Feature => {
        const f = new Feature({
            geometry: new Point(fromLonLat([e.lon, e.lat]))
        });
        f.setId(e.id);
        f.setProperties(e);
        return f;
    }));
}

async function tryQueryOverpass(bbox: Extent, signal: AbortSignal): Promise<Array<any> | false>
{
    try
    {
        return await queryOverpass(bbox, signal).then((r: any): Array<any> => r.elements)
    }
    catch (e)
    {
        console.log(e);
        return false;
    }
}

function getOverpassCompatibleBbox(map: Map): Extent
{
    return transformExtent(map.getView().calculateExtent(map.getSize()), map.getView().getProjection(), "EPSG:4326");
}

type MapLoadState = { status: "idle" } | { status: "loaded"; bbox: Extent } | { status: "loading"; bbox: Extent; controller: AbortController };

export function manageMapOnMove(map: Map, source: VectorSource, minimumZoom: number, bufferFactor: number): () => Promise<void>
{
    let state: MapLoadState = { status: "idle" };
    async function load(bbox: Extent): Promise<void>
    {
        const controller: AbortController = new AbortController();
        const buffered: Extent = bboxBuffer(bbox, map.getView().getZoom()! * bufferFactor);
        state = { status: "loading", bbox: buffered, controller };
        const elements: any[] | false = await tryQueryOverpass(buffered, controller.signal);
        if (elements === false)
            state = { status: "idle" };
        else
        {
            source.clear();
            addElementsToSource(source, elements);
            state = { status: "loaded", bbox: buffered };
        }
    }
    return async (): Promise<void> => {
        const zoom: number = map.getView().getZoom()!;
        const currentBbox: Extent = getOverpassCompatibleBbox(map);
        if (zoom < minimumZoom)
        {
            if (state.status === "loading")
                state.controller.abort();
            source.clear();
            state = { status: "idle" };
        }
        else
            switch (state.status)
            {
                case "idle":
                    await load(currentBbox);
                    break;
                case "loaded":
                    if (!containsExtent(state.bbox, currentBbox))
                        await load(currentBbox);
                    break;
                case "loading":
                    if (!containsExtent(state.bbox, currentBbox))
                    {
                        state.controller.abort();
                        await load(currentBbox);
                    }
                    break;
        }
    };
}