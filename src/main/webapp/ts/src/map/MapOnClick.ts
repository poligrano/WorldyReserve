import {MapEvent, Overlay, Map, MapBrowserEvent, Feature} from "ol";
import {FeatureLike} from "ol/Feature";
import {Utils} from "./Utils";
import {fromLonLat} from "ol/proj";

type PopUpState = { status: "idle" } | { status: "loaded", unload: Function };

async function loadNominatim(feature: FeatureLike): Promise<void>
{
    const poi: any = feature.getProperties();
    if (poi.nominatim === undefined)
    {
        poi.nominatim = await Utils.queryNominatim([poi.lat, poi.lon]);
        (feature as Feature).setProperties(poi);
        console.log(poi);
    }
}

async function loadMeta(feature: FeatureLike): Promise<void>
{
    const poi: any = feature.getProperties();
}

export function manageMapOnClick(overlay: Overlay, popUpCloseButton: HTMLButtonElement): (e: MapBrowserEvent) => void
{
    popUpCloseButton.onclick = unload;
    const popUpNameElem: HTMLParagraphElement = document.getElementById("popup-name") as HTMLParagraphElement;
    const popUpAddrElem: HTMLSpanElement = document.getElementById("popup-address") as HTMLSpanElement;
    let state: PopUpState = { status: "idle" };
    function unload(): void
    {
        overlay.setPosition(undefined);
        popUpNameElem.textContent = "";
        popUpAddrElem.textContent = "";
        state = { status: "idle" };
    }
    async function load(feature: FeatureLike): Promise<void>
    {
        await loadNominatim(feature);
        if (state.status === "loaded")
            render(feature.getProperties());
    }
    function render(poi: any): void
    {
        popUpNameElem.textContent = poi.nominatim.name;
        popUpAddrElem.textContent = poi.nominatim.display_name;
        overlay.setPosition(fromLonLat([poi.lon, poi.lat]));
    }
    return async (e: MapBrowserEvent): Promise<void> => {
        if (state.status === "loaded")
            state.unload();
        const feature: FeatureLike | undefined = e.map.forEachFeatureAtPixel(e.pixel, (f: FeatureLike): FeatureLike => f);
        if (feature !== undefined)
        {
            state = { status: "loaded", unload: unload };
            await load(feature);
        }
    }
}