<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("loginTitle", realm.displayName!realm.name)}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css" />
</head>
<body class="${bodyClass}">
  <div class="crm2-login-card">
    <div class="crm2-welcome">
      <#nested "header">
      <p class="crm2-subtitle">Ingresa tus credenciales para entrar a CRM2</p>
    </div>
    <#if displayMessage && message?has_content>
      <div class="crm2-alert-${message.type}" role="alert">
        ${message.summary?no_esc}
      </div>
    </#if>
    <#nested "form">
    <#if displayInfo>
      <#nested "info">
    </#if>
  </div>
</body>
</html>
</#macro>