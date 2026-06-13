<#--
  Pipely Text Email — Enlace de proveedor de identidad
-->
Pipely — Vincular cuenta de identidad

Hola ${user.firstName!"usuario"},

<#if identityProviderAlias??>
Se solicita vincular tu cuenta de Pipely con el proveedor de identidad: ${identityProviderAlias}.
</#if>

Visita el siguiente enlace para confirmar la vinculación:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo de Pipely

---
Pipely — Tu plataforma de gestión comercial