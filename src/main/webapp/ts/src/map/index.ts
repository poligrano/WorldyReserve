import Map from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import {fromLonLat} from "ol/proj";
import VectorSource from "ol/source/Vector";
import {Feature, MapBrowserEvent, Overlay} from "ol";
import VectorLayer from "ol/layer/Vector";
import {Fill, Icon, Stroke, Style} from "ol/style";
import {Utils} from "./Utils";
import {manageMapOnMove} from "./MapOnMove";
import {manageMapOnClick} from "./MapOnClick";
import {Circle} from "ol/geom";
import {FavouriteManager} from "./FavouriteManager";

export const UserName: string = document.getElementById("username")!.textContent;

function initDropDownMenuEvents(): void
{
    const settingsButtonElem: HTMLButtonElement = document.getElementById("settings-btn") as HTMLButtonElement;
    const dropDownMenuElem: HTMLDivElement = document.getElementById("dropdown") as HTMLDivElement;
    settingsButtonElem.onclick = (): boolean => settingsButtonElem.classList.toggle("active", dropDownMenuElem.classList.toggle("open"));
    document.onclick = (e: PointerEvent) => {
        if (!settingsButtonElem.contains(e.target as Node) && !dropDownMenuElem.contains(e.target as Node))
        {
            settingsButtonElem.classList.remove("active");
            dropDownMenuElem.classList.remove("open");
        }
    }
}

function initSearchBarEvents(): void
{
    const searchBarElem: HTMLInputElement = document.getElementById("search-input") as HTMLInputElement;
    const searchDropDownElem: HTMLDivElement = document.getElementById("search-dropdown") as HTMLDivElement;
    function createFoundElem(found: any): HTMLButtonElement
    {
        const elem: HTMLButtonElement = document.createElement("button");
        elem.className = "dropdown-item";
        elem.textContent = found.display_name;
        elem.onclick = (): void =>
        {
            map.getView().animate({ center: fromLonLat([found.lon, found.lat]), zoom: 15, duration: 600 });
            searchBarElem.value = found.display_name;
        }
        return elem;
    }
    function lastSearchEvent(): () => Promise<void>
    {
        let controller: AbortController | null = null;
        return async (): Promise<void> => {
            const search: string = searchBarElem.value.trim();
            if (search.length !== 0)
            {
                controller?.abort();
                controller = new AbortController();
                searchDropDownElem.replaceChildren(...(await Utils.querySearchNominatim(search, controller.signal)).map((f: any): HTMLButtonElement => createFoundElem(f)));
                searchDropDownElem.classList.add("open");
            }
        }
    }
    const searchEvent: () => Promise<void> = lastSearchEvent();
    searchBarElem.onkeydown = Utils.debounce(() => searchEvent(), 400);
}

export const fav: FavouriteManager = new FavouriteManager();

initDropDownMenuEvents();
initSearchBarEvents();

const src: VectorSource = new VectorSource();
const layer: VectorLayer = new VectorLayer({
    source: src,
    style: new Style({
        image: new Icon({
            src: "data:image/svg+xml;charset=utf-8," + encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="-90 -215 180 230"><ellipse cx="0" cy="12" rx="22" ry="6" fill="#3d1f00" opacity="0.25"/><path d="M0,-210 C-52,-210 -90,-172 -90,-120 C-90,-50 0,18 0,18 C0,18 90,-50 90,-120 C90,-172 52,-210 0,-210 Z" fill="#6b3a1f" stroke="#3d1f00" stroke-width="3"/><rect x="-54" y="-198" width="108" height="210" rx="3" fill="#8b5a2b"/><rect x="-60" y="-210" width="120" height="18" rx="2" fill="#3d1f00"/><rect x="-54" y="-194" width="108" height="4" fill="#3d1f00" opacity="0.4"/><rect x="-46" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-36" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-9" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-186" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-156" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-126" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-96" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-66" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="28" y="-36" width="18" height="22" rx="2" fill="#fde68a" stroke="#b8860b" stroke-width="0.5"/><rect x="-46" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-9" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="28" y="-186" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-46" y="-156" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="28" y="-156" width="9" height="22" fill="#c47a2a" opacity="0.35"/><rect x="-28" y="-28" width="56" height="40" rx="3" fill="#3d1f00"/><rect x="-20" y="-26" width="18" height="38" rx="2" fill="#5a2d0c"/><rect x="2" y="-26" width="18" height="38" rx="2" fill="#5a2d0c"/><rect x="-38" y="-44" width="76" height="18" rx="2" fill="#fde68a"/><text x="0" y="-30" text-anchor="middle" font-family="sans-serif" font-weight="700" font-size="14" fill="#6b3a1f">HOTEL</text><circle cx="-1" cy="-8" r="3" fill="#fde68a"/><circle cx="1" cy="-8" r="3" fill="#fde68a"/></svg>`),
            width: 48,
            height: 60,
            anchor: [0.5, 1]
        })
    })
});

const overlay: Overlay = new Overlay({
    element: document.getElementById("hotel-popup")!,
    positioning: "bottom-center",
    stopEvent: true,
    offset: [0, -16],
});

export const map: Map = new Map({
    layers: [
        new TileLayer({
            source: new OSM(),
        }),
        layer
    ],
    overlays: [
        overlay
    ],
    target: "map",
    view: new View({
        center: [0, 0],
        zoom: 15,
        zoomFactor: 2,
        maxZoom: 20
    })
});

function initGeolocation(): void
{
    const feature: Feature = new Feature();
    map.addLayer(new VectorLayer({
        source: new VectorSource({
            features: [feature]
        }),
        style: new Style({
            fill: new Fill({ color: "rgba(4, 212, 224, 0.8)" }),
            stroke: new Stroke({ color: "rgb(28, 123, 251)" })
        })
    }));
    navigator.geolocation.getCurrentPosition((info: GeolocationPosition): void => map.getView().setCenter(fromLonLat([info.coords.longitude, info.coords.latitude])));
    navigator.geolocation.watchPosition((info: GeolocationPosition): void => feature.setGeometry(new Circle(fromLonLat([info.coords.longitude, info.coords.latitude]), info.coords.accuracy)));
    (document.getElementById("locate-btn") as HTMLButtonElement).onclick = (): void => map.getView().animate({ center: (feature.getGeometry() as Circle).getCenter(), zoom: 15, duration: 600 })
}

initGeolocation();

const mapOnMove: () => Promise<void> = manageMapOnMove(map, src, layer, 13, 5);

map.on("moveend", Utils.debounce((): Promise<void> => mapOnMove(), 1000));

const mapOnClick: (e: MapBrowserEvent) => void = manageMapOnClick(overlay);

map.on("click", (e: MapBrowserEvent): void => mapOnClick(e));

map.on('pointermove', (e: MapBrowserEvent): string => map.getTargetElement().style.cursor = (map.hasFeatureAtPixel(e.pixel) ? "pointer" : "inherit"));