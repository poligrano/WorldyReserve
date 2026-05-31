import {Overlay, MapBrowserEvent, Feature} from "ol";
import {FeatureLike} from "ol/Feature";
import {Utils} from "./Utils";
import {fromLonLat} from "ol/proj";
import {fav, UserName} from "./index";

type PopUpState = { status: "idle" } | { status: "loaded", unload: Function, loaded: Feature };

async function loadNominatim(poi: any): Promise<any | null>
{
    if (fav.getFavourite(poi.id) !== undefined)
        return fav.getFavourite(poi.id);
    if (poi.nominatim === undefined)
        return await Utils.queryReverseNominatim([poi.lat, poi.lon]);
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
    let updated: boolean = false;
    if (nominatim !== null)
    {
        poi.nominatim = nominatim;
        updated = true;
    }
    if (embedded_meta !== null)
    {
        poi.embedded_meta = embedded_meta;
        updated = true;
    }
    if (updated)
        feature.setProperties(poi);
}

function saveNewOwnComment(feature: Feature, comment: string): { comment: string, display_name: string, when_posted: string }
{
    const poi: any = feature.getProperties();
    const new_comment = { comment: comment, display_name: UserName, when_posted: Utils.getCurrentDateStr() };
    (poi.embedded_meta.own_comments as Array<any>).push(new_comment);
    feature.setProperties(poi);
    return new_comment;
}

function createCommentElem(comment: any): HTMLDivElement
{
    const elem: HTMLDivElement = document.createElement("div");
    elem.className = "comment-card";
    elem.innerHTML = `
          <div class="comment-meta">
            <img class="comment-avatar" src="image${comment.leaver_id !== undefined ? `?id=${comment.leaver_id}` : ""}" alt="!" />
            <span class="comment-author">${comment.display_name}</span>
            <span class="comment-date">${comment.when_posted}</span>
          </div>
          <p class="comment-text">${comment.comment}</p>`;
    return elem;
}

export function manageMapOnClick(overlay: Overlay): (e: MapBrowserEvent) => void
{
    const popUpNameElem: HTMLParagraphElement = document.getElementById("popup-name") as HTMLParagraphElement;
    const popUpAddrElem: HTMLSpanElement = document.getElementById("popup-address") as HTMLSpanElement;
    const popUpLikeNumberElem: HTMLSpanElement = document.getElementById("like-count") as HTMLSpanElement;
    const popUpLikeButtonElem: HTMLButtonElement = document.getElementById("like-btn") as HTMLButtonElement;
    const popUpFavButtonElem: HTMLButtonElement = document.getElementById("fav-btn") as HTMLButtonElement;
    const popUpCommentsElem: HTMLDivElement = document.getElementById("comments-list") as HTMLDivElement;
    const popUpCommentTextElem: HTMLInputElement = document.getElementById("new-comment") as HTMLInputElement;
    const popUpReserveAElem: HTMLAnchorElement = document.getElementById("reserve-btn") as HTMLAnchorElement;
    let state: PopUpState = { status: "idle" };
    (document.getElementById("send-comment-btn") as HTMLButtonElement).onclick = async (): Promise<void> => {
        const comment: string = popUpCommentTextElem.value.trim();
        if (comment.length !== 0 && state.status == "loaded")
        {
            const res: string | true =  await Utils.queryPostComment(state.loaded.getId() as number, comment);
            if (res !== true)
                Utils.notifyError(res, 4000);
            else
                renderNewOwnComment(saveNewOwnComment(state.loaded, comment));
        }
    }
    (document.getElementById("popup-close") as HTMLButtonElement).onclick = unload;
    (document.getElementById("like-btn") as HTMLButtonElement).onclick = () => {
        if (state.status === "loaded")
        {
            const poi: any = state.loaded.getProperties();
            poi.embedded_meta.poi.like_number += (poi.embedded_meta.does_like ? -1 : 1);
            poi.embedded_meta.does_like = !poi.embedded_meta.does_like;
            state.loaded.setProperties(poi);
            renderLike(poi.embedded_meta.does_like, poi.embedded_meta.poi.like_number);
        }
    }
    (document.getElementById("fav-btn") as HTMLButtonElement).onclick = () => {
        if (state.status === "loaded")
        {
            const poi: any = state.loaded.getProperties();
            poi.embedded_meta.favourite = !poi.embedded_meta.favourite;
            state.loaded.setProperties(poi);
            if (poi.embedded_meta.favourite)
                fav.addFavourites(poi.nominatim);
            else
                fav.removeFavourite(poi.id);
            renderFavourite(poi.embedded_meta.favourite);
        }
    }
    async function unload(): Promise<void>
    {
        overlay.setPosition(undefined);
        if (state.status === "loaded")
        {
            const res: string | true = await Utils.queryUpdateMeta(state.loaded.getId() as number, state.loaded.getProperties().embedded_meta.does_like, state.loaded.getProperties().embedded_meta.favourite)
            if (res !== true)
                Utils.notifyError(res, 4000);
        }
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
    function renderNewOwnComment(comment: { display_name: string, when_posted: string }): void
    {
        const commentElem: HTMLDivElement = createCommentElem(comment);
        if (popUpCommentsElem.firstChild == null)
            popUpCommentsElem.appendChild(commentElem)
        else
            popUpCommentsElem.firstChild.before(commentElem);
        popUpCommentTextElem.value = "";
    }
    function renderReserved(reserved: boolean): void
    {
        if (reserved)
        {
            popUpReserveAElem.classList.add("reserved");
            popUpReserveAElem.innerHTML = `<i class="fas fa-check"></i> Prenotato`;
            popUpReserveAElem.style.pointerEvents = "none";
        }
        else
        {
            popUpReserveAElem.classList.remove("reserved");
            popUpReserveAElem.innerHTML = `<i class="fas fa-calendar-check"></i> Prenota`;
            popUpReserveAElem.style.pointerEvents = "auto";
        }
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
        renderComments([...(poi.embedded_meta.own_comments as Array<any>).reverse(), ...(poi.embedded_meta.poi.comments as Array<any>).reverse()]);
        renderReserved(poi.embedded_meta.reserved);
        popUpReserveAElem.href = `reserve.jsp?id=${encodeURIComponent(poi.id)}&name=${encodeURIComponent(poi.nominatim.name)}`;
        overlay.setPosition(fromLonLat([poi.lon, poi.lat]));
    }
    return async (e: MapBrowserEvent): Promise<void> => {
        if (state.status === "loaded")
            state.unload();
        const feature: FeatureLike | undefined = e.map.forEachFeatureAtPixel(e.pixel, (f: FeatureLike): FeatureLike => f);
        if (feature !== undefined && feature.getId() !== undefined)
        {
            state = { status: "loaded", unload: unload, loaded: feature as Feature };
            try
            {
                await load(feature);
            }
            catch (e)
            {
                state = { status: "idle" };
            }
        }
    }
}