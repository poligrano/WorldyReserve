import {MapEvent, Overlay, Map, MapBrowserEvent, Feature} from "ol";
import {FeatureLike} from "ol/Feature";
import {Utils} from "./Utils";
import {fromLonLat} from "ol/proj";
import ol from "ol/dist/ol";
import array = ol.array;

type PopUpState = { status: "idle" } | { status: "loaded", unload: Function, loaded: Feature, updated: boolean };

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

function createCommentElem(comment: any): HTMLDivElement
{
    const elem: HTMLDivElement = document.createElement("div");
    elem.className = "comment-card";
    elem.innerHTML = `
          <div class="comment-meta">
            <img class="comment-avatar" src="image?id=${comment.leaver_id}" alt="!" />
            <span class="comment-author">${comment.display_name}</span>
            <span class="comment-date">${comment.when_posted}</span>
          </div>
          <p class="comment-text">${comment.comment}</p>`;
    return elem;
}

export function manageMapOnClick(overlay: Overlay): (e: MapBrowserEvent) => void
{
    (document.getElementById("popup-close") as HTMLButtonElement).onclick = unload;
    (document.getElementById("like-btn") as HTMLButtonElement).onclick = () => {
        if (state.status === "loaded")
        {
            const poi: any = state.loaded.getProperties();
            poi.embedded_meta.poi.like_number += (poi.embedded_meta.does_like ? -1 : 1);
            poi.embedded_meta.does_like = !poi.embedded_meta.does_like;
            state.loaded.setProperties(poi);
            renderLike(poi.embedded_meta.does_like, poi.embedded_meta.poi.like_number);
            state.updated = true;
        }
    }
    (document.getElementById("fav-btn") as HTMLButtonElement).onclick = () => {
        if (state.status === "loaded")
        {
            const poi: any = state.loaded.getProperties();
            poi.embedded_meta.favourite = !poi.embedded_meta.favourite;
            state.loaded.setProperties(poi);
            renderFavourite(poi.embedded_meta.favourite);
            state.updated = true;
        }
    }
    const popUpNameElem: HTMLParagraphElement = document.getElementById("popup-name") as HTMLParagraphElement;
    const popUpAddrElem: HTMLSpanElement = document.getElementById("popup-address") as HTMLSpanElement;
    const popUpLikeNumberElem: HTMLSpanElement = document.getElementById("like-count") as HTMLSpanElement;
    const popUpLikeButtonElem: HTMLButtonElement = document.getElementById("like-btn") as HTMLButtonElement;
    const popUpFavButtonElem: HTMLButtonElement = document.getElementById("fav-btn") as HTMLButtonElement;
    const popUpCommentsElem: HTMLDivElement = document.getElementById("comments-list") as HTMLDivElement;
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
    function renderLike(does_like: boolean, like_number: number): void
    {
        popUpLikeNumberElem.textContent = like_number.toString();
        popUpLikeButtonElem.classList.toggle("liked", does_like);
        popUpLikeButtonElem.querySelector("i")!.className = (does_like ? "fas fa-heart" : "far fa-heart");
    }
    function renderFavourite(favourite: boolean): void
    {
        popUpFavButtonElem.classList.toggle("favorited", favourite);
        popUpFavButtonElem.querySelector("i")!.className = (favourite ? "fas fa-bookmark" : "far fa-bookmark");
    }
    function renderComments(comments: Array<any>): void
    {
        popUpCommentsElem.replaceChildren(...comments.map((c: any): HTMLDivElement => createCommentElem(c)));
    }
    function render(poi: any): void
    {
        console.log(poi);
        popUpNameElem.textContent = poi.nominatim.name;
        popUpAddrElem.textContent = poi.nominatim.display_name;
        renderLike(poi.embedded_meta.does_like, poi.embedded_meta.poi.like_number);
        renderFavourite(poi.embedded_meta.favourite);
        renderComments([...(poi.embedded_meta.own_comments as Array<any>), ...(poi.embedded_meta.poi.comments as Array<any>)]);
        overlay.setPosition(fromLonLat([poi.lon, poi.lat]));
    }
    return async (e: MapBrowserEvent): Promise<void> => {
        if (state.status === "loaded")
            state.unload();
        const feature: FeatureLike | undefined = e.map.forEachFeatureAtPixel(e.pixel, (f: FeatureLike): FeatureLike => f);
        if (feature !== undefined)
        {
            state = { status: "loaded", unload: unload, loaded: feature as Feature, updated: false };
            await load(feature);
        }
    }
}