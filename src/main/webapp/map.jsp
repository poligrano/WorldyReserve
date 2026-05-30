<jsp:useBean id="name" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) == null || pageContext.request.getSession(false).getAttribute("uid") == null}'>
    <c:redirect url="index.jsp" />
</c:if>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
    <title>Progetto TPSIT</title>
    <link rel="stylesheet" href="css/map.css">
    <link rel="stylesheet" href="ts/node_modules/@fortawesome/fontawesome-free/css/all.min.css" />
</head>
<body>
<header>
    <img src="image" class="avatar" alt="!">
    <span class="username" id="username">${name}</span>
    <div class="header-spacer"></div>
    <div class="header-actions">
        <div class="dropdown-wrap">
            <button class="icon-btn" id="settings-btn" title="Impostazioni">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
            </button>
            <div class="dropdown" id="dropdown">
                <a href="/profile" class="dropdown-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    Profilo
                </a>
                <div class="dropdown-divider"></div>
                <a href="logout" class="dropdown-item dropdown-item--danger">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                    Logout
                </a>
            </div>
        </div>
    </div>
</header>
<div id="map-container">
    <div id="map"></div>
</div>
<div id="hotel-popup" class="hotel-popup">
    <div class="popup-header">
        <div class="popup-header-img">
            <button class="close-btn" id="popup-close">
                <i class="fas fa-xmark"></i>
            </button>
            <div class="hotel-title-area">
                <p class="hotel-name" id="popup-name"></p>
                <span class="hotel-location">
                    <i class="fas fa-location-dot" style="font-size:12px;"></i>
                    <span id="popup-address"></span>
                </span>
            </div>
        </div>
    </div>
    <div class="popup-body">
        <div class="action-bar">
            <button class="action-btn" id="like-btn">
                <i class="far fa-heart"></i> Mi Piace <span class="count" id="like-count">0</span>
            </button>
            <button class="action-btn" id="fav-btn">
                <i class="far fa-bookmark"></i> Salva
            </button>
        </div>
        <div class="divider"></div>
        <div class="comments-section">
            <p class="section-title"><i class="far fa-comment"></i> Commenti</p>
            <div id="comments-list"></div>
            <div class="comment-input-row">
                <input class="comment-input" id="new-comment" type="text" placeholder="Commenta..." maxlength="255" />
                <button class="send-btn" id="send-comment-btn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    </div>
    <div class="divider"></div>
    <div class="popup-footer">
        <a href="reserve.jsp" class="reserve-btn" id="reserve-btn">
            <i class="fas fa-calendar-check"></i> Prenota
        </a>
    </div>
</div>
<div id="toast">
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="var(--error)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
    </svg>
    <span id="toast-msg"></span>
</div>
</body>
</html>
<script src="ts/dist/map.js"></script>