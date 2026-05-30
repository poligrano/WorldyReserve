import {init_error} from "./generic_error";

const beginElem: HTMLInputElement = document.getElementById("start") as HTMLInputElement;
const endElem: HTMLInputElement = document.getElementById("end") as HTMLInputElement;

export function onSubmitCheck(): boolean
{
    const begin: number = beginElem.valueAsDate!.valueOf();
    const end: number = endElem.valueAsDate!.valueOf();
    if (begin < Date.now() || end < begin)
    {
        init_error("Date di prenotazione invalide");
        return false;
    }
    return true;
}