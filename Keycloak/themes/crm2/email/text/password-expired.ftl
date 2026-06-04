<#--
  CRM2 Text Email — Contraseña expirada
-->
CRM2 — Tu contraseña ha expirado

Hola ${user.firstName!"usuario"},

Tu contraseña ha expirado. Para continuar usando tu cuenta, debés crear una nueva contraseña.

Visitá el siguiente enlace para crear una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo CRM2

---
CRM2 — Tu plataforma de gestión comercial