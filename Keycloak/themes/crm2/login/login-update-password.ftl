<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("updatePasswordTitle", "Actualizar contraseña")}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css" />
</head>
<body>
  <div class="crm2-login-card">
    <div class="crm2-welcome">
      <h1>${msg("updatePasswordTitle", "Actualizar contraseña")}</h1>
      <p class="crm2-subtitle">Crea una nueva contraseña para tu cuenta</p>
    </div>
    <form id="kc-form-login" class="crm2-form" action="${url.loginAction}" method="post">
      <div class="crm2-form-group">
        <label for="password-new">Nueva contraseña</label>
        <input type="password" id="password-new" name="password-new" class="crm2-input" autofocus autocomplete="new-password" />
      </div>
      <div class="crm2-form-group">
        <label for="password-confirm">Confirmar contraseña</label>
        <input type="password" id="password-confirm" name="password-confirm" class="crm2-input" autocomplete="new-password" />
      </div>
      <input type="submit" value="${msg("submit", "Guardar nueva contraseña")}" class="crm2-btn-primary" />
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
  </div>
</body>
</html>