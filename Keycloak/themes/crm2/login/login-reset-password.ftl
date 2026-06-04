<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("resetPasswordTitle", "Restablecer contraseña")}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css" />
</head>
<body>
  <div class="crm2-login-card">
    <div class="crm2-welcome">
      <h1>${msg("resetPasswordTitle", "Restablecer contraseña")}</h1>
      <p class="crm2-subtitle">Recibirás un enlace para crear una nueva contraseña</p>
    </div>
    <form id="kc-form-login" class="crm2-form" action="${url.loginAction}" method="post">
      <div class="crm2-form-group">
        <label for="username">Correo</label>
        <input type="text" id="username" name="username" class="crm2-input" placeholder="tu@correo.com" autofocus autocomplete="username" />
      </div>
      <input type="submit" value="${msg("submit", "Enviar enlace")}" class="crm2-btn-primary" />
      <#if message?has_content>
        <div class="crm2-alert-error" role="alert">
          <#if message.type = "error">
            ${message.summary?replace('<a ', '<a target="_blank" rel="noopener" ')}
          <#else>
            ${message.summary}
          </#if>
        </div>
      </#if>
    </form>
    <div class="crm2-back-link-wrapper">
      <a href="${url.loginUrl}" class="crm2-back-link">Volver al inicio de sesión</a>
    </div>
  </div>
</body>
</html>