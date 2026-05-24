const emailElement: HTMLInputElement = document.getElementById("email") as HTMLInputElement;
const sendButtonElement: HTMLInputElement = document.getElementById("send") as HTMLInputElement;
const timerElement: HTMLElement = document.getElementById("timer");

async function sendCodeEvent(URI: string, delay: number): Promise<void>
{
    let resp: Response = await fetch(URI + "?email=" + emailElement.value);
    alert(await resp.text());
    if (resp.ok)
        timeoutSend(delay);
}

function timeoutSend(delay: number): void
{
    sendButtonElement.disabled = true;
    const timer = setInterval(() => {
        delay--;
        timerElement.innerHTML = "00:" + (delay.toString().length === 1 ? "0" : "") + delay;
        if (delay === 0)
        {
            sendButtonElement.disabled = false;
            clearInterval(timer);
        }
    }, 1000);
}