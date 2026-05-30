import {Extent} from "ol/extent";
import {Coordinate} from "ol/coordinate";

export namespace Utils
{
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
    export async function queryNominatim(coord: Coordinate, signal: AbortSignal | null = null): Promise<any>
    {
        console.log("Querying Nominatim", coord);
        return fetch(`https://nominatim.openstreetmap.org/reverse?lat=${coord[0]}&lon=${coord[1]}&format=json&zoom=18`, {
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
        return fetch(`${ProjectServerURL}/getmeta?id=${id}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r) => r.json());
    }
    export async function queryUpdateMeta(id: number, does_like: boolean, favourite: boolean, signal: AbortSignal | null = null): Promise<string | true>
    {
        console.log("Querying Project Servlet Meta Update", does_like, favourite);
        return fetch(`${ProjectServerURL}/updatemeta?id=${id}&does_like=${does_like}&favourite=${favourite}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then(async (r: Response): Promise<string | true> => (r.ok ? true : await r.text()));
    }
    export async function queryPostComment(id: number, comment: string, signal: AbortSignal | null = null): Promise<string | true>
    {
        console.log("Querying Project Servlet Post Comment", comment);
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
}