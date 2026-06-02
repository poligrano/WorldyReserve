import VectorSource from "ol/source/Vector";
import VectorLayer from "ol/layer/Vector";
import {Stroke, Style} from "ol/style";
import {Feature, Map} from "ol";
import {Utils} from "./Utils";
import {Coordinate} from "ol/coordinate";

type GoToState = { status: "idle" } | { status: "open", coord: Coordinate } | { status: "loading", coord: Coordinate, controller: AbortController } | { status: "loaded", coord: Coordinate, route: Feature };

export class GoToManager
{
    private readonly resumeElem: HTMLDivElement = document.getElementById("navigation-panel") as HTMLDivElement;
    private readonly modesElem: NodeListOf<HTMLInputElement> = document.getElementsByName("travel-mode") as NodeListOf<HTMLInputElement>;
    private readonly info: { dist: HTMLSpanElement, eta: HTMLSpanElement, arrivalTime: HTMLSpanElement } = { dist: document.getElementById("summary-distance") as HTMLSpanElement, eta: document.getElementById("summary-eta") as HTMLSpanElement, arrivalTime: document.getElementById("summary-arrival-time") as HTMLSpanElement };
    private readonly src: VectorSource = new VectorSource();
    private coord: Coordinate;
    private state: GoToState = { status: "idle" };
    constructor(map: Map, coord: Coordinate)
    {
        this.coord = coord;
        map.addLayer(new VectorLayer({
            source: this.src,
            style: new Style({
                stroke: new Stroke({
                    width: 7,
                    color: "rgba(0, 69, 221, 0.75)"
                })
            })
        }));
        (document.getElementById("nav-close-btn") as HTMLButtonElement).onclick = (): void => this.closePanel();
        this.modesElem.forEach((e: HTMLInputElement) => e.onclick = (): Promise<void> => this.beginRoute());
    }
    public openPanel(to: Coordinate): void
    {
        this.closePanel();
        this.resumeElem.classList.add("open");
        this.state = { status: "open", coord: to };
        this.getChecked().click();
    }
    private closePanel(): void
    {
        this.stopRoute();
        this.resumeElem.classList.remove("open");
        this.state = { status: "idle" };
    }
    private stopRoute(): void
    {
        if (this.state.status !== "idle")
        {
            this.src.clear();
            if (this.state.status === "loading")
                this.state.controller.abort();
            else
                this.clearInfo();
            this.state = { status: "open", coord: this.state.coord };
        }
    }
    private async beginRoute(): Promise<void>
    {
        this.stopRoute();
        if (this.state.status === "open")
        {
            this.state = {status: "loading", coord: this.state.coord, controller: new AbortController()};
            const osrm: any = await Utils.queryOSRM(this.coord, this.state.coord, this.getCheckedMode(), this.state.controller.signal);
            if (osrm.code !== "Ok")
            {
                Utils.notifyError("Indicazioni non trovate!", 4000);
                this.state = { status: "open", coord: this.state.coord };
            }
            else
            {
                this.renderInfo(osrm.routes[0]);
                this.state = { status: "loaded", coord: this.state.coord, route: this.drawRoute(osrm.routes[0].geometry) };
            }
        }
    }
    private clearInfo(): void
    {
        this.info.dist.textContent = "Caricamento...";
        this.info.eta.textContent = "Caricamento...";
        this.info.arrivalTime.textContent = "--:--";
    }
    private renderInfo(osrm: any): void
    {
        this.info.dist.textContent = this.calculateDistance(osrm.distance);
        this.info.eta.textContent = this.calculateDuration(osrm.duration);
        this.info.arrivalTime.textContent = this.calculateArrivalTime(osrm.duration);
    }
    private calculateDistance(distance: number): string
    {
        const km: number = distance / 1000;
        return (km >= 1 ? `${km.toFixed(1)} km (` : "") + `${distance.toFixed(0)} m` + (km >= 1 ? ")" : "");
    }
    private calculateDuration(duration: number): string
    {
        const h: number = Math.trunc(duration / (60 * 60));
        duration -= h * 60 * 60;
        const m: number = Math.trunc(duration / 60);
        return (h >= 1 ? `${h} h ` : "") + (m >= 1 ? m : Math.ceil(m)) + " min";
    }
    private calculateArrivalTime(duration: number): string
    {
        const time = new Date();
        time.setSeconds(time.getSeconds() + duration);
        return time.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
        });
    }
    private drawRoute(polyline: any): Feature
    {
        const feature = new Feature({ geometry: Utils.polyToGeom(polyline) });
        this.src.addFeature(feature);
        return feature;
    }
    private getChecked(): HTMLInputElement
    {
        return this.modesElem.values().find((e: HTMLInputElement): boolean => e.checked)!;
    }
    private getCheckedMode(): "car" | "foot" | "bike"
    {
        return this.getChecked().value as "car" | "foot" | "bike";
    }
    public onUpdatePosition(coord: Coordinate): void
    {
        this.coord = coord;
    }
}