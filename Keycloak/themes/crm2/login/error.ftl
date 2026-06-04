<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("errorTitle", "Error")}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css" />
</head>
<body>
  <div class="crm2-login-card">
    <div class="crm2-welcome">
      <h1>${msg("errorTitle", "Error")}</h1>
      <p class="crm2-subtitle">Se produjo un error</p>
    </div>
    <#if message??>
      <div class="crm2-alert-error" role="alert">
        <#if message.type = "error">
          ${message.summary?replace('<a ', '<a target="_blank" rel="noopener" ')}
        <#else>
          ${message.summary}
        </#if>
      </div>
    </#if>
    <div class="crm2-back-link-wrapper">
      <a href="${url.loginUrl}" class="crm2-back-link">Volver al inicio de sesión</a>
    </div>
  </div>
</body>
</html>