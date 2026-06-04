<#--
  CRM2 Text Email — Ejecutar acciones
-->
CRM2 — Acción requerida en tu cuenta

Hola ${user.firstName!"usuario"},

El administrador ha solicitado la siguiente acción en tu cuenta:
<#if requiredActions??>
${requiredActions}
</#if>

Visitá el siguiente enlace para completar esta acción:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo CRM2

---
CRM2 — Tu plataforma de gestión comercial