<#--
  CRM2 Text Email — Ejecutar acciones
-->
CRM2 — Acción requerida en tu cuenta

Hola ${user.firstName!"usuario"},

El administrador ha solicitado la siguiente acción en tu cuenta:
<#if requiredActions??>
<#list requiredActions as reqAction>
<#if reqAction == "UPDATE_PASSWORD">actualizar tu contraseña<#elseif reqAction == "VERIFY_EMAIL">verificar tu correo electrónico<#elseif reqAction == "CONFIGURE_TOTP">configurar tu autenticador<#else>completar la acción: ${reqAction}</#if><#sep>, </#sep>
</#list>
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