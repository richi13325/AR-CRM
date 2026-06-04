<#--
  CRM2 Text Email — Enlace de proveedor de identidad
-->
CRM2 — Vincular cuenta de identidad

Hola ${user.firstName!"usuario"},

<#if identityProviderAlias??>
Se solicita vincular tu cuenta CRM2 con el proveedor de identidad: ${identityProviderAlias}.
</#if>

Visitá el siguiente enlace para confirmar la vinculación:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo CRM2

---
CRM2 — Tu plataforma de gestión comercial