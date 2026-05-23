async function sendCodeEvent(URI: string, delay: number): Promise<void>
{
    let resp: Response = await fetch(URI + "?email=" + (document.getElementById("email") as HTMLInputElement).value);
    alert(await resp.text());
    if (resp.ok)
        timeoutSend(delay);
}

function timeoutSend(delay: number): void
{
    (document.getElementById("send") as HTMLInputElement).disabled = true;
    const timer = setInterval(() => {
        delay--;
        document.getElementById("timer").innerHTML = "00:" + (delay.toString().length === 1 ? "0" : "") + delay;
        if (delay === 0)
        {
            (document.getElementById("send") as HTMLInputElement).disabled = false;
            clearInterval(timer);
        }
    }, 1000);
}