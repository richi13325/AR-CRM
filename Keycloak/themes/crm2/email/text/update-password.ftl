<#--
  Pipely Text Email — Actualizar contraseña
-->
Pipely — Actualiza tu contraseña

Hola ${user.firstName!"usuario"},

Se te solicita actualizar la contraseña de tu cuenta de Pipely en ${realmName}.

Visita el siguiente enlace para establecer una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo de Pipely

---
Pipely — Tu plataforma de gestión comercial