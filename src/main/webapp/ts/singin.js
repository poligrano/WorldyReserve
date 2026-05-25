var pfpElement = document.getElementById("pfp");
function pfpOnChange(maxSize) {
    if (pfpElement.files[0].size > maxSize) {
        init_error("Foto profilo troppo grande");
        pfpElement.value = "";
    }
}
