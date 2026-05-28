import Map from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import {fromLonLat, transformExtent} from "ol/proj";
import VectorSource from "ol/source/Vector";
import {Feature, MapEvent} from "ol";
import {Point} from "ol/geom";
import VectorLayer from "ol/layer/Vector";
import {containsExtent, Extent} from "ol/extent";
import {Circle, Fill, Icon, Image, Style} from "ol/style";

const hotelMarkerSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="-90 -215 180 230"><ellipse cx="0" cy="12" rx="22" ry="6" fill="#3d1f00" opacity="0.25"/><path d="M0,-210 C-52,-210 -90,-172 -90,-120 C-90,-50 0,18 0,18 C0,18 90,-50 90,-120 C90,-172 52,-210 0,-210 Z" fill="#6b3a1f" stroke="#3d1f00" stroke-width="3"/><rect x="-54" y="-198" width="108" height="210" rx="3" fill="#8b5a2b"/><rect x="-60" y="-210" width="120" height="18" rx="2" fill="#3d1f00"/><rect x="-54" y="-194" width="108" height="4" fill="#3d1f00" opacity="0.4"/><rect x="-46" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-36" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-36" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-9" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="28" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-46" y="-156" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="28" y="-156" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-28" y="-28" width="56" height="40" rx="3" fill="#3d1f00"/><rect x="-20" y="-26" width="18" height="38" rx="2" fill="#5a2d0c"/><rect x="2" y="-26" width="18" height="38" rx="2" fill="#5a2d0c"/><rect x="-38" y="-44" width="76" height="18" rx="2" fill="#fde68a"/><text x="0" y="-30" text-anchor="middle" font-family="sans-serif" font-weight="700" font-size="14" fill="#6b3a1f">HOTEL</text><circle cx="-1" cy="-8" r="3" fill="#fde68a"/><circle cx="1" cy="-8" r="3" fill="#fde68a"/></svg>`;

const src: VectorSource = new VectorSource();
const layer: VectorLayer = new VectorLayer({
    source: src,
    style: new Style({
        image: new Icon({
            src: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(hotelMarkerSvg),
            width: 48,
            height: 60,
            anchor: [0.5, 1]
        })
    })
});

export const map = new Map({
    layers: [
        new TileLayer({
            source: new OSM(),
        }),
        layer
    ],
    target: 'map',
    view: new View({
        center: [0, 0],
        zoom: 15,
        zoomFactor: 2,
        maxZoom: 20
    })
});

export function get_bbox(): Extent
{
    return transformExtent(map.getView().calculateExtent(map.getSize()), map.getView().getProjection(), "EPSG:4326");
}

navigator.geolocation.getCurrentPosition((c) => map.getView().setCenter(fromLonLat([c.coords.longitude, c.coords.latitude])));

async function query_overpass(bbox: Extent, signal: AbortSignal): Promise<any>
{
    console.log("Querying Overpass, bbox: ", bbox);
    const [minLon, minLat, maxLon, maxLat] = bbox;
    const query = `[out:json][timeout:20];
                    node(${minLat},${minLon},${maxLat},${maxLon})[tourism=hotel];
                    out qt;`;
    const overpassAPI = "https://overpass-api.de/api/interpreter";
    return await fetch(overpassAPI, {
        method: "POST",
        signal: signal,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body:`data=${encodeURIComponent(query)}`
    }).then((r) => {
        console.log("Parsing response");
        return r.json();
    });
}

function parse_result(elements: Array<any>)
{
    console.log("Parsing result, elements: " + elements);
    src.addFeatures(elements!.map((v) => {
        let f = new Feature({
            geometry: new Point(fromLonLat([v.lon, v.lat]))
        });
        f.setId(v.id);
        f.setProperties(v);
        return f;
    }));
}

async function reload_from_bbox(bbox: Extent, signal: AbortSignal): Promise<boolean>
{
    try
    {
        old_bbox = bbox;
        const elements = await query_overpass(bbox, signal).then((r) => r.elements);
        src.clear();
        parse_result(elements);
        return true;
    }
    catch (e)
    {
        console.error(e);
        return false;
    }
}

let old_bbox: Extent | undefined = undefined;

function reset_pois(bbox: Extent | undefined)
{
    src.clear();
    old_bbox = bbox;
}

function buffer_percentage(bbox: Extent, perc: number) {
    const w = (bbox[2] - bbox[0]) * perc / 100;
    const h = (bbox[3] - bbox[1]) * perc / 100;
    return [bbox[0] - w, bbox[1] - h, bbox[2] + w, bbox[3] + w];
}

async function map_on_move()
{
    let bbox = get_bbox();
    const zoom = map.getView().getZoom()!;
    if (zoom < 13)
        reset_pois(undefined);
    else if (old_bbox === undefined || !containsExtent(old_bbox, bbox))
    {
        if (abort !== undefined)
            abort.abort();
        abort = new AbortController();
        if (!await reload_from_bbox((bbox = buffer_percentage(bbox, zoom * 2.5)), abort.signal))
            old_bbox = undefined;
    }
}

function debounce(callback: Function, timer: number)
{
    let t: number;
    return (...args: any[]) => {
        clearTimeout(t);
        // @ts-ignore
        t = setTimeout(() => callback.apply(this, args), timer);
    };
}

let abort: AbortController | undefined = undefined;

map.on("moveend", debounce((e: MapEvent) => map_on_move(), 500));