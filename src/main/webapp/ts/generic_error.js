var buttonElement = document.getElementById("error_button");
var flashElement = document.getElementById("error_flash");
var mxElement = document.getElementById("error_mx");
function init_error(mx) {
    mxElement.innerHTML = mx;
    flashElement.style.display = "flex";
    buttonElement.onclick = function () {
        flashElement.style.display = "none";
    };
}
