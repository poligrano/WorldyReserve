import {Extent} from "ol/extent";
import {Coordinate} from "ol/coordinate";
import {Polyline} from "ol/format";
import {Geometry} from "ol/geom";

export type RestResult = { ok: boolean, mx: string };

export namespace Utils
{
    //const ProjectServerURL = "http://192.168.0.136:8080/ProgettoTPSIT_war_exploded";
    const ProjectServerURL = "http://localhost:8080/ProgettoTPSIT_war_exploded";
    export function debounce(callback: Function, timer: number): (...args: any[]) => void
    {
        let t: number;
        return (...args: any[]): void => {
            clearTimeout(t);
            // @ts-ignore
            t = setTimeout((): any => callback.apply(...args), timer);
        };
    }
    export async function queryOverpass(bbox: Extent, signal: AbortSignal | null = null): Promise<any>
    {
        console.log("Querying Overpass", bbox);
        const [minLon, minLat, maxLon, maxLat] = bbox;
        const query = `[bbox:${minLat},${minLon},${maxLat},${maxLon}][out:json][timeout:5];
                    node[tourism=hotel];
                    out skel qt;`;
        return fetch("https://overpass-api.de/api/interpreter", {
            method: "POST",
            signal: signal,
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "User-Agent": navigator.userAgent
            },
            body:`data=${encodeURIComponent(query)}`
        }).then((r: Response): Promise<any> => r.json());
    }
    export async function queryReverseNominatim(coord: Coordinate, signal: AbortSignal | null = null): Promise<any>
    {
        console.log("Querying Nominatim Reverse", coord);
        return fetch(`https://nominatim.openstreetmap.org/reverse?lat=${encodeURIComponent(coord[0])}&lon=${encodeURIComponent(coord[1])}&format=json&zoom=18`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r) => r.json());
    }
    export async function queryMetaServlet(id: number, signal: AbortSignal | null = null): Promise<any>
    {
        console.log("Querying Project Servlet Meta GET", id);
        return fetch(`${ProjectServerURL}/getmeta?id=${encodeURIComponent(id)}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r) => r.json());
    }
    export async function queryUpdateMeta(id: number, does_like: boolean, favourite: boolean, signal: AbortSignal | null = null): Promise<RestResult>
    {
        console.log("Querying Project Servlet Meta Update", id, does_like, favourite);
        return fetch(`${ProjectServerURL}/updatemeta?id=${encodeURIComponent(id)}&does_like=${encodeURIComponent(does_like)}&favourite=${encodeURIComponent(favourite)}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then(async (r: Response): Promise<RestResult> => { return { ok: r.ok, mx: await r.text() } });
    }
    export async function queryPostComment(id: number, comment: string, signal: AbortSignal | null = null): Promise<RestResult>
    {
        console.log("Querying Project Servlet Post Comment", id, comment);
        return fetch(`${ProjectServerURL}/updatemeta`, {
            signal: signal,
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "User-Agent": navigator.userAgent
            },
            body: `id=${encodeURIComponent(id)}&comment=${encodeURIComponent(comment)}`
        }).then(async (r: Response): Promise<RestResult> => { return { ok: r.ok, mx: await r.text() } });
    }
    export async function querySearchNominatim(search: string, signal: AbortSignal | null = null): Promise<Array<any>>
    {
        console.log("Querying Nominatim Search", search);
        return Promise.all([fetch(`https://nominatim.openstreetmap.org/search?q=hotel ${encodeURIComponent(search)}&format=json&limit=5`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r: Response) => r.json()),
            fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(search)}&format=json&featureType=city&limit=3`, {
                signal: signal,
                method: "GET",
                headers: {
                    "User-Agent": navigator.userAgent
                }
            }).then((r: Response) => r.json())]).then((r: [any, any]) => [...r[0], ...r[1]]);
    }
    export async function queryByIdNominatim(id: number, signal: AbortSignal | null = null): Promise<any>
    {
        console.log("Querying Nominatim By Id", id);
        return fetch(`https://nominatim.openstreetmap.org/lookup?osm_ids=N${encodeURIComponent(id)}&format=json`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r) => r.json());
    }
    export async function queryOSRM(w1: Coordinate, w2: Coordinate, type: "car" | "foot" | "bike", signal: AbortSignal | null = null)
    {
        console.log("Querying OSRM", w1, w2, type);
        return fetch(`https://routing.openstreetmap.de/routed-${encodeURIComponent(type)}/route/v1/driving/${encodeURIComponent(w1[0])},${encodeURIComponent(w1[1])};${encodeURIComponent(w2[0])},${encodeURIComponent(w2[1])}?overview=full&geometries=polyline`, {
            signal: signal,
            method: "GET"
        }).then((r) => r.json());
    }
    export async function queryDeleteComment(id: number, signal: AbortSignal | null = null): Promise<RestResult>
    {
        console.log("Querying Project Servlet Delete Comment", id);
        return fetch(`${ProjectServerURL}/updatemeta?id=${encodeURIComponent(id)}`, {
            signal: signal,
            method: "DELETE",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then(async (r: Response): Promise<RestResult> => { return { ok: r.ok, mx: await r.text() } });
    }
    function initNotifyError(): (mx: string, duration: number) => void
    {
        let timer: number | undefined = undefined;
        const err: HTMLDivElement = document.getElementById("toast") as HTMLDivElement;
        const errMx: HTMLSpanElement = document.getElementById("toast-msg") as HTMLSpanElement;
        return (mx: string, duration: number): void => {
            clearTimeout(timer);
            err.classList.remove("show");
            errMx.textContent = mx;
            err.classList.add("show");
            timer = setTimeout((): void => err.classList.remove("show"), duration);
        }
    }
    export const notifyError: (mx: string, duration: number) => void = initNotifyError();
    export function getLocaleDateTime(epoch: number | null = null): string
    {
        const date = (epoch === null ? new Date() : new Date(epoch));
        return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
    }
    export function isMobile(): boolean
    {
        return window.innerWidth <= 600;
    }
    function initPolyToGeom(): (polyline: any) => Geometry
    {
        const poly = new Polyline({ factor: 1e5 });
        return (polyline: any): Geometry => poly.readGeometry(polyline, {
            dataProjection: "EPSG:4326",
            featureProjection: "EPSG:3857"
        });
    }
    export const polyToGeom: (polyline: any) => Geometry = initPolyToGeom();
}