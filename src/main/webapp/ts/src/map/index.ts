import Map from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import {fromLonLat} from "ol/proj";
import VectorSource from "ol/source/Vector";
import {MapBrowserEvent, MapEvent, Overlay} from "ol";
import VectorLayer from "ol/layer/Vector";
import {Icon, Style} from "ol/style";
import {Utils} from "./Utils";
import {manageMapOnMove} from "./MapOnMove";
import {manageMapOnClick} from "./MapOnClick";
import {FeatureLike} from "ol/Feature";

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

const overlay = new Overlay({
    element: document.getElementById("hotel-popup")!,
    positioning: "bottom-center",
    stopEvent: true,
    offset: [0, -16],
});

export const map = new Map({
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

navigator.geolocation.getCurrentPosition((c: GeolocationPosition): void => map.getView().setCenter(fromLonLat([c.coords.longitude, c.coords.latitude])));

const mapOnMove: () => Promise<void> = manageMapOnMove(map, src, layer, 13, 5);

map.on("moveend", Utils.debounce((e: MapEvent): Promise<void> => mapOnMove(), 1000));

const mapOnClick: (e: MapBrowserEvent) => void = manageMapOnClick(overlay);

map.on("click", (e: MapBrowserEvent): void => mapOnClick(e));

map.on('pointermove', (e: MapBrowserEvent): string => map.getTargetElement().style.cursor = (map.hasFeatureAtPixel(e.pixel) ? "pointer" : "inherit"));