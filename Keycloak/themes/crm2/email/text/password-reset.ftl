<#--
  Pipely Text Email — Restablecer contraseña
-->
Pipely — Restablecer contraseña

Hola ${user.firstName!"usuario"},

Se solicitó un restablecimiento de contraseña para tu cuenta en ${realmName}.

Visita el siguiente enlace para crear una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Si no solicitaste este cambio, puedes ignorar este mensaje. Tu contraseña seguirá siendo la misma.

Saludos,
El equipo de Pipely

---
Pipely — Tu plataforma de gestión comercial