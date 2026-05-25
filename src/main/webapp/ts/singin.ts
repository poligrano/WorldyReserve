const pfpElement: HTMLInputElement = document.getElementById("pfp") as HTMLInputElement;

function pfpOnChange(maxSize: number): void
{
    if (pfpElement.files[0].size > maxSize)
    {
        init_error("Foto profilo troppo grande");
        pfpElement.value = "";
    }
}