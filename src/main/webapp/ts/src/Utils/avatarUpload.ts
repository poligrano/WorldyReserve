import {initError} from "./genericError";

export function initAvatar(maxSize: number): void
{
    const pfpElement: HTMLInputElement = document.getElementById("photoInput") as HTMLInputElement;
    const pfpShowElement: HTMLImageElement = document.getElementById("avatarImg") as HTMLImageElement;
    (document.getElementById("pfpChange") as HTMLDivElement).onclick = (document.getElementById("pfpChangePencil") as HTMLDivElement).onclick = (): void => pfpElement.click();
    const fileReader = new FileReader();
    fileReader.onload = (): string => pfpShowElement.src = fileReader.result as string
    pfpElement.onchange = (): void => {
        const newPfp: File = pfpElement.files![0];
        if (newPfp.size > maxSize)
        {
            initError("Foto profilo troppo grande");
            pfpElement.value = "";
        }
        else
            fileReader.readAsDataURL(newPfp);
    }
}