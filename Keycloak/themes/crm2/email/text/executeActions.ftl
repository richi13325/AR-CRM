<#--
  Pipely Text Email — Ejecutar acciones
-->
Pipely — Acción requerida en tu cuenta

Hola ${user.firstName!"usuario"},

El administrador ha solicitado la siguiente acción en tu cuenta:
<#if requiredActions??>
<#list requiredActions as reqAction>
<#if reqAction == "UPDATE_PASSWORD">actualizar tu contraseña<#elseif reqAction == "VERIFY_EMAIL">verificar tu correo electrónico<#elseif reqAction == "CONFIGURE_TOTP">configurar tu autenticador<#else>completar la acción: ${reqAction}</#if><#sep>, </#sep>
</#list>
</#if>

Visita el siguiente enlace para completar esta acción:

${link}

<#if linkExpiration??>
Este enlace expira en ${linkExpiration}.
</#if>

Saludos,
El equipo de Pipely

---
Pipely — Tu plataforma de gestión comercial