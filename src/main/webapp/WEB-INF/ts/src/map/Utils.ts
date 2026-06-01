import {Extent} from "ol/extent";
import {Coordinate} from "ol/coordinate";

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
        const query = `[out:json][timeout:10];
                    node(${minLat},${minLon},${maxLat},${maxLon})[tourism=hotel];
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
    export async function queryUpdateMeta(id: number, does_like: boolean, favourite: boolean, signal: AbortSignal | null = null): Promise<string | true>
    {
        console.log("Querying Project Servlet Meta Update", id, does_like, favourite);
        return fetch(`${ProjectServerURL}/updatemeta?id=${encodeURIComponent(id)}&does_like=${encodeURIComponent(does_like)}&favourite=${encodeURIComponent(favourite)}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then(async (r: Response): Promise<string | true> => (r.ok ? true : await r.text()));
    }
    export async function queryPostComment(id: number, comment: string, signal: AbortSignal | null = null): Promise<string | true>
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
        }).then(async (r: Response): Promise<string | true> => (r.ok ? true : await r.text()));
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
    export function getCurrentDateStr(): string
    {
        return (new Date()).toISOString().substring(0, 10);
    }
    export function isMobile(): boolean
    {
        return window.innerWidth <= 600;
    }
}