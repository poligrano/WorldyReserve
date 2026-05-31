import {fromLonLat} from "ol/proj";
import {map} from "./index";
import {Utils} from "./Utils";

export class FavouriteManager
{
    readonly savedButtonElem: HTMLButtonElement = document.getElementById("hotels-btn") as HTMLButtonElement;
    readonly dropDownElem: HTMLDivElement = document.getElementById("hotels-dropdown") as HTMLDivElement;
    readonly favourite: Map<number, any> = new Map;
    constructor()
    {
        this.savedButtonElem.onclick = (): boolean => this.savedButtonElem.classList.toggle("active", this.dropDownElem.classList.toggle("open"));
        document.onclick = (e: PointerEvent): void => {
            if (!this.savedButtonElem.contains(e.target as Node) && !this.dropDownElem.contains(e.target as Node))
            {
                this.savedButtonElem.classList.remove("active");
                this.dropDownElem.classList.remove("open");
            }
        }
    }
    createSavedElem(saved: any): HTMLButtonElement
    {
        console.log("Parsing Saved", saved);
        const elem: HTMLButtonElement = document.createElement("button");
        elem.className = "dropdown-item";
        elem.id = `osm_id${saved.osm_id}`;
        elem.textContent = saved.display_name;
        elem.onclick = () => map.getView().animate({ center: fromLonLat([saved.lon, saved.lat]), zoom: 15, duration: 600 });
        return elem;
    }
    public async addFromIds(...ids: number[]): Promise<void>
    {
        ids.forEach((id: number): Promise<void> => Utils.queryByIdNominatim(id).then((e: any): void => this.addFavourites(...e)));
    }
    public addFavourites(...any: any[]): void
    {
        any.forEach((e: any): Map<number, any> => this.favourite.set(e.osm_id, e));
        this.dropDownElem.append(...any.map((e: any): HTMLButtonElement => this.createSavedElem(e)));
    }
    public getFavourite(id: number): any | undefined
    {
        return this.favourite.get(id);
    }
    public removeFavourite(id: number): void
    {
        document.getElementById(`osm_id${id}`)!.remove();
        this.favourite.delete(id);
    }
}