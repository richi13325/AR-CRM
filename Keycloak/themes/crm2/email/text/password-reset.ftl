<#--
  CRM2 Text Email — Restablecer contraseña
-->
CRM2 — Restablecer contraseña

Hola ${user.firstName!"usuario"},

Se solicitó un restablecimiento de contraseña para tu cuenta en ${realmName}.

Visitá el siguiente enlace para crear una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Si no solicitaste este cambio, podés ignorar este mensaje. Tu contraseña seguirá siendo la misma.

Saludos,
El equipo CRM2

---
CRM2 — Tu plataforma de gestión comercial