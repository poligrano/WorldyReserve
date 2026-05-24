var pfpElement = document.getElementById("pfp");
function pfpOnChange(maxSize) {
    if (pfpElement.files[0].size > maxSize) {
        alert("Foto profilo troppo pesante!");
        pfpElement.value = "";
    }
}
