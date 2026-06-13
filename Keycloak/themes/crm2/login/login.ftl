<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("loginTitle", realm.displayName!realm.name)}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css" />
</head>
<body>
  <div class="crm2-login-card">
    <div class="crm2-welcome">
      <h1>${msg("loginTitle", "Iniciar sesión")}</h1>
      <p class="crm2-subtitle">Ingresa tus credenciales para entrar a Pipely</p>
    </div>
    <form id="kc-form-login" class="crm2-form" action="${url.loginAction}" method="post">
      <#if usernameEditDisabled??>
        <div class="crm2-form-group">
          <label for="username">Correo</label>
          <input type="text" id="username" name="username" value="${login.username!''}" class="crm2-input" disabled autocomplete="username" />
        </div>
      <#else>
        <div class="crm2-form-group">
          <label for="username">Correo</label>
          <input type="text" id="username" name="username" value="${login.username!''}" class="crm2-input" placeholder="tu@correo.com" autofocus autocomplete="username" />
        </div>
      </#if>
      <div class="crm2-form-group">
        <label for="password">Contraseña</label>
        <input type="password" id="password" name="password" class="crm2-input" autocomplete="current-password" />
      </div>
      <div class="crm2-form-options">
        <#if realm.rememberMe && !usernameEditDisabled??>
          <label class="crm2-checkbox">
            <input type="checkbox" name="rememberMe" value="true" <#if login.rememberMe??>checked</#if> />
            Recordarme
          </label>
        <#else>
          <span></span>
        </#if>
        <#if realm.password>
          <a href="${url.loginResetCredentialsUrl}" class="crm2-forgot-link">¿Olvidaste tu contraseña?</a>
        </#if>
      </div>
      <input type="submit" value="${msg("login", "Iniciar sesión")}" class="crm2-btn-primary" />
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