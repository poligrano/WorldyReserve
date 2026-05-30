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
    <style>
        .dropdown-wrap {
            position: relative;
        }

        .dropdown {
            position: absolute;
            top: calc(100% + 10px);
            right: 0;
            min-width: 160px;
            background: rgba(30, 19, 8, 0.96);
            border: 1px solid var(--border);
            border-radius: 10px;
            box-shadow:
                    0 0 0 1px rgba(200,121,65,0.08),
                    0 8px 32px rgba(0,0,0,0.55);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            overflow: hidden;
            opacity: 0;
            transform: translateY(-6px) scale(0.97);
            pointer-events: none;
            transition: opacity 0.18s ease, transform 0.18s ease;
        }

        .dropdown.open {
            opacity: 1;
            transform: translateY(0) scale(1);
            pointer-events: all;
        }

        .dropdown::before {
            content: '';
            position: absolute;
            top: 0; left: 12%; right: 12%;
            height: 1px;
            background: linear-gradient(90deg, transparent, var(--accent), transparent);
            opacity: 0.5;
        }

        .dropdown-item {
            display: flex;
            align-items: center;
            gap: 0.6rem;
            padding: 0.65rem 1rem;
            font-size: 0.88rem;
            font-weight: 500;
            color: var(--text);
            text-decoration: none;
            transition: background 0.15s, color 0.15s;
        }
        .dropdown-item svg {
            width: 15px; height: 15px;
            flex-shrink: 0;
            color: var(--muted);
            transition: color 0.15s;
        }
        .dropdown-item:hover {
            background: rgba(200,121,65,0.08);
            color: var(--accent-h);
        }
        .dropdown-item:hover svg { color: var(--accent); }

        .dropdown-item--danger { color: var(--error); }
        .dropdown-item--danger svg { color: var(--error); opacity: 0.7; }
        .dropdown-item--danger:hover {
            background: rgba(192,92,58,0.12);
            color: #e07055;
        }
        .dropdown-item--danger:hover svg { color: #e07055; opacity: 1; }

        .dropdown-divider {
            height: 1px;
            background: var(--border);
            margin: 0 0.75rem;
            opacity: 0.6;
        }

        .hotel-popup {
            background: #FAF6F1;
            border-radius: 14px;
            width: 360px;
            border: 2px solid #DEC8A8;
            overflow: hidden;
            box-shadow: 0 8px 32px rgba(61,40,16,0.22);
            font-family: system-ui, sans-serif;
        }

        .hotel-popup::after {
            content: '';
            position: absolute;
            bottom: -12px;
            left: 50%;
            transform: translateX(-50%);
            border: 12px solid transparent;
            border-top-color: #DEC8A8;
            border-bottom: 0;
        }

        .popup-header-img {
            width: 100%;
            height: 120px;
            background: linear-gradient(160deg, #6B4A2A 0%, #3D2810 100%);
            display: flex;
            align-items: flex-end;
            padding: 12px 14px;
            position: relative;
        }

        .hotel-badge {
            background: #A0734A;
            color: #F0E6D6;
            font-size: 11px;
            font-weight: 500;
            padding: 3px 8px;
            border-radius: 20px;
            letter-spacing: 0.05rem;
            position: absolute;
            top: 12px;
            left: 14px;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .stars {
            display: flex;
            gap: 2px;
            color: #E5B84A;
            font-size: 14px;
            position: absolute;
            top: 10px;
            left: 50%;
            transform: translateX(-50%);
        }

        .close-btn {
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(255,255,255,0.15);
            border: none;
            border-radius: 50%;
            width: 28px;
            height: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: #F0E6D6;
            font-size: 16px;
            transition: background 0.15s;
        }
        .close-btn:hover { background: rgba(255,255,255,0.28); }

        .hotel-title-area { padding: 0 14px 12px; }

        .hotel-name {
            font-size: 17px;
            font-weight: 500;
            color: #F0E6D6;
            margin: 0 0 2px;
        }

        .hotel-location {
            font-size: 12px;
            color: #DEC8A8;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .popup-body {
            padding: 14px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .action-bar {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .action-btn {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 7px 14px;
            border-radius: 20px;
            border: 1px solid #DEC8A8;
            background: white;
            color: #6B4A2A;
            font-size: 13px;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.15s, color 0.15s;
            font-family: inherit;
        }
        .action-btn i { font-size: 16px; }
        .action-btn:hover { background: #F0E6D6; }
        .action-btn.liked   { background: #FCEBEB; color: #A32D2D; border-color: #F09595; }
        .action-btn.favorited { background: #FAEEDA; color: #854F0B; border-color: #FAC775; }

        .count { font-size: 12px; color: #A0734A; margin-left: 2px; }

        .info-section {
            background: #F0E6D6;
            border-radius: 10px;
            padding: 12px;
        }

        .section-title {
            font-size: 11px;
            font-weight: 500;
            color: #6B4A2A;
            text-transform: uppercase;
            letter-spacing: 0.6px;
            margin: 0 0 10px;
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px;
        }

        .info-item { display: flex; flex-direction: column; gap: 2px; }

        .info-label { font-size: 11px; color: #A0734A; }

        .info-value {
            font-size: 13px;
            font-weight: 500;
            color: #3D2810;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .info-value i { font-size: 14px; color: #A0734A; }

        .score-pill {
            background: #A0734A;
            color: #FAF6F1;
            font-size: 13px;
            font-weight: 500;
            padding: 2px 8px;
            border-radius: 20px;
            display: inline-block;
        }

        .comments-section { display: flex; flex-direction: column; gap: 8px; }

        .comment-card {
            background: white;
            border: 1px solid #F0E6D6;
            border-radius: 8px;
            padding: 8px 10px;
        }

        .comment-meta {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 4px;
        }

        .comment-avatar {
            width: 22px;
            height: 22px;
            border-radius: 50%;
            object-fit: cover;
            flex-shrink: 0;
        }

        .comment-author { font-size: 12px; font-weight: 500; color: #3D2810; flex: 1; }
        .comment-date   { font-size: 11px; color: #A0734A; }
        .comment-text   { font-size: 12px; color: #6B4A2A; line-height: 1.5; margin: 0; }

        .comment-input-row { display: flex; gap: 6px; align-items: center; }

        .comment-input {
            flex: 1;
            border: 1px solid #DEC8A8;
            border-radius: 20px;
            padding: 7px 12px;
            font-size: 13px;
            background: white;
            color: #3D2810;
            outline: none;
            font-family: inherit;
        }
        .comment-input::placeholder { color: #DEC8A8; }
        .comment-input:focus { border-color: #A0734A; }

        .send-btn {
            background: #6B4A2A;
            border: none;
            border-radius: 50%;
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: #FAF6F1;
            font-size: 16px;
            flex-shrink: 0;
            transition: background 0.15s;
        }
        .send-btn:hover { background: #3D2810; }

        .divider { height: 1px; background: #F0E6D6; }

        .popup-footer {
            padding: 10px 14px;
            display: flex;
            gap: 8px;
            justify-content: flex-end;
        }

        .view-btn {
            background: transparent;
            color: #6B4A2A;
            font-size: 13px;
            font-weight: 500;
            padding: 8px 16px;
            border-radius: 20px;
            border: 1px solid #DEC8A8;
            cursor: pointer;
            font-family: inherit;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: background 0.15s;
        }
        .view-btn:hover { background: #F0E6D6; }

        .reserve-btn {
            background: #6B4A2A;
            color: #FAF6F1;
            font-size: 13px;
            font-weight: 500;
            padding: 8px 18px;
            border-radius: 20px;
            border: none;
            cursor: pointer;
            font-family: inherit;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: background 0.15s;
        }
        .reserve-btn:hover { background: #3D2810; }
        .reserve-btn.reserved {
            background: #27500A;
            color: #EAF3DE;
        }
    </style>
</head>
<body>
<header>
    <img src="image" class="avatar" alt="!">
    <span class="username">${name}</span>
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
                <input class="comment-input" id="new-comment" type="text" placeholder="Commenta..." />
                <button class="send-btn" id="send-comment-btn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    </div>
    <div class="divider"></div>
    <div class="popup-footer">
        <button class="reserve-btn" id="reserve-btn">
            <i class="far fa-bookmark"></i> Prenota
        </button>
    </div>
</div>
</body>
</html>
<script src="ts/dist/map.js"></script>