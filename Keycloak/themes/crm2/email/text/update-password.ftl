<#--
  CRM2 Text Email — Actualizar contraseña
-->
CRM2 — Actualiza tu contraseña

Hola ${user.firstName!"usuario"},

Se te solicita actualizar la contraseña de tu cuenta CRM2 en ${realmName}.

Visitá el siguiente enlace para establecer una nueva contraseña:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo CRM2

---
CRM2 — Tu plataforma de gestión comercial