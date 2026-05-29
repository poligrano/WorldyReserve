import {Extent} from "ol/extent";
import {Coordinate} from "ol/coordinate";

export namespace Utils
{
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
        console.log("Querying Project Servlet", id);
        return fetch(`http://localhost:8080/ProgettoTPSIT_war_exploded/getmeta?id=${id}`, {
            signal: signal,
            method: "GET",
            headers: {
                "User-Agent": navigator.userAgent
            }
        }).then((r) => r.json());
    }
}