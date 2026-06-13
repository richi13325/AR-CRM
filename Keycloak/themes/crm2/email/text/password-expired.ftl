<#--
  Pipely Text Email — Contraseña expirada
-->
Pipely — Tu contraseña ha expirado

Hola ${user.firstName!"usuario"},

Tu contraseña ha expirado. Para continuar usando tu cuenta, debes crear una nueva contraseña.

Visita el siguiente enlace para crear una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo de Pipely

---
Pipely — Tu plataforma de gestión comercial