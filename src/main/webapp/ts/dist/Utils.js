var Utils;
/******/ (() => { // webpackBootstrap
/******/ 	"use strict";
/******/ 	var __webpack_modules__ = ({

/***/ "./src/Utils/change_password.ts"
/*!**************************************!*\
  !*** ./src/Utils/change_password.ts ***!
  \**************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   sendCodeEvent: () => (/* binding */ sendCodeEvent)
/* harmony export */ });
/* harmony import */ var _generic_error__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./generic_error */ "./src/Utils/generic_error.ts");

const emailElement = document.getElementById("email");
const sendButtonElement = document.getElementById("send");
const timerElement = document.getElementById("timer");
async function sendCodeEvent(URI, delay) {
    let resp = await fetch(URI + "?email=" + emailElement.value);
    (0,_generic_error__WEBPACK_IMPORTED_MODULE_0__.init_error)(await resp.text());
    if (resp.ok)
        timeoutSend(delay);
}
function timeoutSend(delay) {
    sendButtonElement.disabled = true;
    timerElement.innerHTML = "00:" + (delay.toString().length === 1 ? "0" : "") + delay;
    const timer = setInterval(() => {
        delay--;
        timerElement.innerHTML = "00:" + (delay.toString().length === 1 ? "0" : "") + delay;
        if (delay === 0) {
            timerElement.innerHTML = "";
            sendButtonElement.disabled = false;
            clearInterval(timer);
        }
    }, 1000);
}


/***/ },

/***/ "./src/Utils/generic_error.ts"
/*!************************************!*\
  !*** ./src/Utils/generic_error.ts ***!
  \************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   init_error: () => (/* binding */ init_error)
/* harmony export */ });
const buttonElement = document.getElementById("error_button");
const flashElement = document.getElementById("error_flash");
const mxElement = document.getElementById("error_mx");
function init_error(mx) {
    mxElement.innerHTML = mx;
    flashElement.style.display = "flex";
    buttonElement.onclick = () => {
        flashElement.style.display = "none";
    };
}


/***/ },

/***/ "./src/Utils/singin.ts"
/*!*****************************!*\
  !*** ./src/Utils/singin.ts ***!
  \*****************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   pfpOnChange: () => (/* binding */ pfpOnChange)
/* harmony export */ });
/* harmony import */ var _generic_error__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./generic_error */ "./src/Utils/generic_error.ts");

const pfpElement = document.getElementById("pfp");
function pfpOnChange(maxSize) {
    if (pfpElement.files[0].size > maxSize) {
        (0,_generic_error__WEBPACK_IMPORTED_MODULE_0__.init_error)("Foto profilo troppo grande");
        pfpElement.value = "";
    }
}


/***/ }

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			// no module.id needed
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		if (!(moduleId in __webpack_modules__)) {
/******/ 			delete __webpack_module_cache__[moduleId];
/******/ 			var e = new Error("Cannot find module '" + moduleId + "'");
/******/ 			e.code = 'MODULE_NOT_FOUND';
/******/ 			throw e;
/******/ 		}
/******/ 		__webpack_modules__[moduleId](module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
/******/ 	/* webpack/runtime/define property getters */
/******/ 	(() => {
/******/ 		// define getter functions for harmony exports
/******/ 		__webpack_require__.d = (exports, definition) => {
/******/ 			for(var key in definition) {
/******/ 				if(__webpack_require__.o(definition, key) && !__webpack_require__.o(exports, key)) {
/******/ 					Object.defineProperty(exports, key, { enumerable: true, get: definition[key] });
/******/ 				}
/******/ 			}
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/hasOwnProperty shorthand */
/******/ 	(() => {
/******/ 		__webpack_require__.o = (obj, prop) => (Object.prototype.hasOwnProperty.call(obj, prop))
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/make namespace object */
/******/ 	(() => {
/******/ 		// define __esModule on exports
/******/ 		__webpack_require__.r = (exports) => {
/******/ 			if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 				Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 			}
/******/ 			Object.defineProperty(exports, '__esModule', { value: true });
/******/ 		};
/******/ 	})();
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry needs to be wrapped in an IIFE because it needs to be isolated against other modules in the chunk.
(() => {
/*!****************************!*\
  !*** ./src/Utils/index.ts ***!
  \****************************/
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   init_error: () => (/* reexport safe */ _generic_error__WEBPACK_IMPORTED_MODULE_0__.init_error),
/* harmony export */   pfpOnChange: () => (/* reexport safe */ _singin__WEBPACK_IMPORTED_MODULE_2__.pfpOnChange),
/* harmony export */   sendCodeEvent: () => (/* reexport safe */ _change_password__WEBPACK_IMPORTED_MODULE_1__.sendCodeEvent)
/* harmony export */ });
/* harmony import */ var _generic_error__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./generic_error */ "./src/Utils/generic_error.ts");
/* harmony import */ var _change_password__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./change_password */ "./src/Utils/change_password.ts");
/* harmony import */ var _singin__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./singin */ "./src/Utils/singin.ts");





})();

Utils = __webpack_exports__;
/******/ })()
;