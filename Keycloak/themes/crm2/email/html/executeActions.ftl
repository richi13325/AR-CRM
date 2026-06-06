<#--
  CRM2 Email Template — Ejecutar acciones (password-change requerido)
  Keycloak standard variables: ${link}, ${realmName}, ${user.email}, ${user.firstName},
  ${linkExpiration}, ${requiredActions}
-->
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Acción requerida — CRM2</title>
  <!--[if mso]>
  <style type="text/css">
    table { border-collapse: collapse; }
    .button { padding: 14px 0 !important; }
  </style>
  <![endif]-->
</head>
<body style="margin:0;padding:0;background:#f4f6f8;font-family:'Inter',system-ui,-apple-system,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;border-collapse:collapse;">
    <tr>
      <td align="center" style="padding:48px 20px;">
        <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%;">
          <tr>
            <td style="background:#ffffff;border:1px solid #d9e2ec;border-radius:11px;overflow:hidden;">
              <!-- Header band -->
              <table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="background:#1e293b;padding:28px 36px;text-align:center;">
                    <span style="font-size:18px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;">CRM2</span>
                  </td>
                </tr>
              </table>
              <!-- Body -->
              <table width="100%" cellpadding="0" cellspacing="0" style="padding:36px 36px 32px;">
                <tr>
                  <td>
                    <h2 style="font-size:18px;font-weight:700;color:#1e293b;margin-bottom:16px;">Acción requerida en tu cuenta</h2>
                    <#if user.firstName??>
                      <p style="font-size:15px;color:#475569;line-height:1.6;margin-bottom:16px;">Hola <strong>${user.firstName}</strong>,</p>
                    </#if>
                    <p style="font-size:15px;color:#475569;line-height:1.6;margin-bottom:16px;">
                      El administrador ha solicitado la siguiente acción en tu cuenta:
                    </p>
                    <#if requiredActions??>
                      <table cellpadding="0" cellspacing="0" width="100%" style="margin-bottom:20px;">
                        <tr>
                          <td style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:12px 14px;">
                            <p style="font-size:14px;font-weight:600;color:#1e293b;margin:0;">
                              <#list requiredActions as reqAction>
                                <#if reqAction == "UPDATE_PASSWORD">actualizar tu contraseña<#elseif reqAction == "VERIFY_EMAIL">verificar tu correo electrónico<#elseif reqAction == "CONFIGURE_TOTP">configurar tu autenticador<#else>completar la acción: ${reqAction}</#if><#sep>, </#sep>
                              </#list>
                            </p>
                          </td>
                        </tr>
                      </table>
                    </#if>
                    <!-- CTA Button -->
                    <table cellpadding="0" cellspacing="0" width="100%" style="margin-bottom:24px;">
                      <tr>
                        <td align="center">
                          <a href="${link}" class="button" style="background:#1e293b;color:#ffffff;padding:14px 32px;border-radius:8px;font-size:14px;font-weight:600;text-decoration:none;display:inline-block;">Completar acción</a>
                        </td>
                      </tr>
                    </table>
                    <!-- Link fallback -->
                    <p style="font-size:13px;color:#94a3b8;margin-bottom:8px;">Si el botón no funciona, copiá y pegá este enlace:</p>
                    <table cellpadding="0" cellspacing="0" width="100%" style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:12px 14px;margin-bottom:16px;">
                      <tr>
                        <td style="font-size:12px;color:#64748b;word-break:break-all;">${link}</td>
                      </tr>
                    </table>
                    <#if linkExpiration??>
                      <p style="font-size:12px;color:#94a3b8;line-height:1.5;">Este enlace expira en ${linkExpiration}.</p>
                    </#if>
                  </td>
                </tr>
              </table>
              <!-- Footer -->
              <table width="100%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:16px 36px;">
                <tr>
                  <td style="text-align:center;">
                    <p style="font-size:12px;color:#94a3b8;">CRM2 — Gestión Comercial</p>
                    <p style="font-size:12px;color:#94a3b8;margin-top:4px;">
                      <a href="#" style="color:#64748b;text-decoration:none;">Política de privacidad</a>&nbsp;·&nbsp;
                      <a href="#" style="color:#64748b;text-decoration:none;">Términos de servicio</a>
                    </p>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>