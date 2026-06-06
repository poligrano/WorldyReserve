import {Overlay, MapBrowserEvent, Feature} from "ol";
import {FeatureLike} from "ol/Feature";
import {RestResult, Utils} from "./Utils";
import {fromLonLat} from "ol/proj";
import {fav, goto, UserName} from "./index";

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

function saveNewOwnComment(feature: Feature, comment: string, comment_id: number): { comment: string, display_name: string, when_posted: number, comment_id: number }
{
    const poi: any = feature.getProperties();
    const new_comment = { comment: comment, display_name: UserName, when_posted: Math.trunc(Date.now() / 1000), comment_id: comment_id };
    (poi.embedded_meta.own_comments as Array<any>).push(new_comment);
    feature.setProperties(poi);
    return new_comment;
}

function createCommentElem(comment: any, own: boolean): HTMLDivElement
{
    const elem: HTMLDivElement = document.createElement("div");
    elem.className = "comment-card";
    elem.innerHTML = `
          <div class="comment-meta">
            <img class="comment-avatar" src="image${comment.leaver_id !== undefined ? `?id=${comment.leaver_id}` : ""}" alt="!" />
            <span class="comment-author">${comment.display_name}</span>
            <span class="comment-date">${Utils.getLocaleDateTime(comment.when_posted * 1000)}</span>
          </div>
          <p class="comment-text">${comment.comment}</p>`;
    if (own)
        appendDeleteButton(elem, comment.comment_id);
    return elem;
}

function appendDeleteButton(card: HTMLDivElement, id: number): void
{
    const btn: HTMLButtonElement = document.createElement("button");
    btn.type = "button";
    btn.className = "delete-comment-btn";
    btn.innerHTML = "<i class=\"fas fa-trash-alt\"></i>";
    btn.onclick = async (): Promise<void> => {
        const res: RestResult = await Utils.queryDeleteComment(id);
        if (!res.ok)
            Utils.notifyError(res.mx, 4000);
        else
            card.remove();
    }
    card.querySelector("div")!.append(btn);
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
    const popUpContentElem: HTMLDivElement = document.getElementById("popup-content") as HTMLDivElement;
    const popUpLoaderElem:HTMLImageElement = document.getElementById("popup-loader-logo") as HTMLImageElement;
    const popupElem: HTMLDivElement = document.getElementById("hotel-popup") as HTMLDivElement;
    let state: PopUpState = { status: "idle" };
    (document.getElementById("goto-btn") as HTMLButtonElement).onclick = async (): Promise<void> => {
        if (state.status === "loaded")
        {
            if (goto === undefined)
                Utils.notifyError("Funzione non disponibile", 4000);
            else
            {
                const loaded = state.loaded;
                state.unload();
                goto.openPanel([loaded.getProperties().lon, loaded.getProperties().lat]);
            }
        }
    }
    (document.getElementById("send-comment-btn") as HTMLButtonElement).onclick = async (): Promise<void> => {
        const comment: string = popUpCommentTextElem.value.trim();
        if (comment.length !== 0 && state.status == "loaded")
        {
            const res: RestResult =  await Utils.queryPostComment(state.loaded.getId() as number, comment);
            if (!res.ok)
                Utils.notifyError(res.mx, 4000);
            else
                renderNewOwnComment(saveNewOwnComment(state.loaded, comment, Number.parseInt(res.mx)));
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
    (document.getElementById("fav-btn") as HTMLButtonElement).onclick = (): void => {
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
    window.addEventListener("resize", () => {
        const changed: boolean = ((): boolean => {
            if (!Utils.isMobile())
            {
                if (overlay.getElement() === undefined)
                {
                    overlay.setElement(popupElem);
                    return true;
                }
            }
            else if (overlay.getElement() !== undefined)
            {
                overlay.setElement(undefined);
                document.body.append(popupElem);
                return true;
            }
            return false;
        })();
        if (state.status === "loaded" && changed)
            showPopUp(state.loaded.getProperties());
    })
    function showPopUp(poi: any): void
    {
        if (overlay.getElement() === undefined)
            popupElem.style.display = "block";
        else
            overlay.setPosition(fromLonLat([poi.lon, poi.lat]));
    }
    function hidePopUp(): void
    {
        if (overlay.getElement() === undefined)
            popupElem.style.display = "none";
        else
            overlay.setPosition(undefined);
    }
    function unload(): void
    {
        hidePopUp()
        popUpLoaderElem.style.display = "inherit";
        popUpContentElem.style.display = "none";
        if (state.status === "loaded" && state.loaded.getProperties().embedded_meta !== undefined)
            Utils.queryUpdateMeta(state.loaded.getId() as number, state.loaded.getProperties().embedded_meta.does_like, state.loaded.getProperties().embedded_meta.favourite).then((r: RestResult)=> {
                if (!r.ok)
                    Utils.notifyError(r.mx, 4000);
            });
        state = { status: "idle" };
    }
    async function load(feature: FeatureLike): Promise<void>
    {
        const poi: any = feature.getProperties();
        showPopUp(poi);
        const [nominatim, embedded_meta] = await Promise.all([loadNominatim(poi), loadMeta(poi)]);
        saveInFeature(feature as Feature, poi, nominatim, embedded_meta);
        if (state.status === "loaded" && state.loaded === feature)
            render(feature.getProperties());
    }
    function renderNewOwnComment(comment: any): void
    {
        const commentElem: HTMLDivElement = createCommentElem(comment, true);
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
        popUpCommentsElem.replaceChildren(...comments.map((c: any): HTMLDivElement => createCommentElem(c, c.comment_id !== undefined)));
    }
    function render(poi: any): void
    {
        popUpNameElem.textContent = poi.nominatim.name;
        popUpAddrElem.textContent = poi.nominatim.display_name;
        renderLike(poi.embedded_meta.does_like, poi.embedded_meta.poi.like_number);
        renderFavourite(poi.embedded_meta.favourite);
        renderComments([...(poi.embedded_meta.own_comments as Array<any>).reverse(), ...(poi.embedded_meta.poi.comments as Array<any>).reverse()]);
        renderReserved(poi.embedded_meta.reserved);
        popUpReserveAElem.href = `reserve.jsp?id=${encodeURIComponent(poi.id)}&name=${encodeURIComponent(poi.nominatim.name)}`;
        popUpLoaderElem.style.display = "none";
        popUpContentElem.style.display = "inherit";
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