import {init_error} from "./generic_error";

const pfpElement: HTMLInputElement = document.getElementById("pfp") as HTMLInputElement;

export function pfpOnChange(maxSize: number): void
{
    if (pfpElement.files![0].size > maxSize)
    {
        init_error("Foto profilo troppo grande");
        pfpElement.value = "";
    }
}