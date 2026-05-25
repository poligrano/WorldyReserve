const buttonElement: HTMLButtonElement = document.getElementById("error_button") as HTMLButtonElement;
const flashElement: HTMLDivElement = document.getElementById("error_flash") as HTMLDivElement;
const mxElement: HTMLElement = document.getElementById("error_mx");

function init_error(mx: string)
{
    mxElement.innerHTML = mx;
    flashElement.style.display = "flex";
    buttonElement.onclick = (): void => {
        flashElement.style.display = "none";
    };
}