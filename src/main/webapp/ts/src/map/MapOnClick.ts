import {MapEvent, Overlay, Map, MapBrowserEvent, Feature} from "ol";
import {FeatureLike} from "ol/Feature";
import {Utils} from "./Utils";
import {fromLonLat} from "ol/proj";

type PopUpState = { status: "idle" } | { status: "loaded", unload: Function, loaded: Feature };

async function loadNominatim(poi: any): Promise<any | null>
{
    if (poi.nominatim === undefined)
        return await Utils.queryNominatim([poi.lat, poi.lon]);
    return null;
}

async function loadMeta(poi: any): Promise<any | null>
{
    if (poi.embedded_meta === undefined)
        return await Utils.queryMetaServlet(poi.id);
    return null;
}

function saveInFeature(feature: Feature, poi: any, nominatim: any | null, embedded_meta: any | null): void
{
    let updated: boolean;
    if ((updated = nominatim !== null))
        poi.nominatim = nominatim;
    if ((updated = embedded_meta !== null))
        poi.embedded_meta = embedded_meta;
    if (updated)
        feature.setProperties(poi);
}

export function manageMapOnClick(overlay: Overlay): (e: MapBrowserEvent) => void
{
    (document.getElementById("popup-close") as HTMLButtonElement).onclick = unload;
    (document.getElementById("like-btn") as HTMLButtonElement).onclick = () => {
        if (state.status === "loaded")
        {
            const poi: any = state.loaded.getProperties();
            const liked: boolean = poi.embedded_meta.does_like;
            poi.embedded_meta.does_like = !liked;
            poi.embedded_meta.poi.like_number += (liked ? -1 : 1);
            state.loaded.setProperties(poi);
            renderLike(poi.embedded_meta);
        }
    }
    const popUpNameElem: HTMLParagraphElement = document.getElementById("popup-name") as HTMLParagraphElement;
    const popUpAddrElem: HTMLSpanElement = document.getElementById("popup-address") as HTMLSpanElement;
    const popUpLikeNumberElem: HTMLSpanElement = document.getElementById("like-count") as HTMLSpanElement;
    const popUpLikeButtonElem: HTMLButtonElement = document.getElementById("like-btn") as HTMLButtonElement;
    let state: PopUpState = { status: "idle" };
    function unload(): void
    {
        overlay.setPosition(undefined);
        state = { status: "idle" };
    }
    async function load(feature: FeatureLike): Promise<void>
    {
        const poi: any = feature.getProperties();
        const [nominatim, embedded_meta] = await Promise.all([loadNominatim(poi), loadMeta(poi)]);
        saveInFeature(feature as Feature, poi, nominatim, embedded_meta);
        if (state.status === "loaded")
            render(feature.getProperties());
    }
    function renderLike(embedded_meta: any): void
    {
        popUpLikeNumberElem.textContent = embedded_meta.poi.like_number;
        popUpLikeButtonElem.className = "action-btn " + (embedded_meta.does_like ? "liked" : "");
        popUpLikeButtonElem.querySelector("i")!.className = (embedded_meta.does_like ? 'fas fa-heart' : 'far fa-heart');
    }
    function render(poi: any): void
    {
        popUpNameElem.textContent = poi.nominatim.name;
        popUpAddrElem.textContent = poi.nominatim.display_name;
        renderLike(poi.embedded_meta);
        overlay.setPosition(fromLonLat([poi.lon, poi.lat]));
    }
    return async (e: MapBrowserEvent): Promise<void> => {
        if (state.status === "loaded")
            state.unload();
        const feature: FeatureLike | undefined = e.map.forEachFeatureAtPixel(e.pixel, (f: FeatureLike): FeatureLike => f);
        if (feature !== undefined)
        {
            state = { status: "loaded", unload: unload, loaded: feature as Feature };
            await load(feature);
        }
    }
}