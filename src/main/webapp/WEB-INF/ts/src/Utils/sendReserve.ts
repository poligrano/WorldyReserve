const startValElem: HTMLInputElement = document.getElementById("start-val") as HTMLInputElement;
const endValElem: HTMLInputElement = document.getElementById("end-val") as HTMLInputElement;
const startElem: HTMLInputElement = document.getElementById("start") as HTMLInputElement;
const endElem: HTMLInputElement = document.getElementById("end") as HTMLInputElement;

export function initSendReserve(): void
{
    startElem.onchange = (): string => startValElem.value = Math.trunc(new Date(startElem.value).getTime() / 1000).toString();
    endElem.onchange = (): string => endValElem.value = Math.trunc(new Date(endElem.value).getTime() / 1000).toString();
}