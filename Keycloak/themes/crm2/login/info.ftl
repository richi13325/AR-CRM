<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=false displayWelcome=false; section>
    <#if section = "form">
        <div id="kc-info-message" style="display:flex;flex-direction:column;gap:20px;">
            <p class="crm2-subtitle" style="margin-top:-12px; text-align:center;">
                ${kcSanitize(message.summary)?no_esc}<#if requiredActions??><#list requiredActions>: <strong><#items as reqActionItem>${kcSanitize(msg("requiredAction.${reqActionItem}"))?no_esc}<#sep>, </#items></strong></#list></#if>
            </p>

            <#if !skipLink??>
                <#if pageRedirectUri?has_content>
                    <a href="${pageRedirectUri}" class="crm2-back-link" style="text-align:center;">${msg("backToApplication")}</a>
                <#elseif actionUri?has_content>
                    <a href="${actionUri}" class="crm2-btn-primary" style="display:block;text-align:center;text-decoration:none;">${msg("proceedWithAction")}</a>
                <#elseif (client.baseUrl)?has_content>
                    <a href="${client.baseUrl}" class="crm2-back-link" style="text-align:center;">${msg("backToApplication")}</a>
                </#if>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>
