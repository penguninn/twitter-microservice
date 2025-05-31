<#import "template.ftl" as layout>
<#import "user-profile-commons.ftl" as userProfileCommons>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign up</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'twitter-blue': '#1DA1F2',
                    }
                }
            }
        }
    </script>
</head>
<body class="h-screen w-screen bg-black text-white">
<div class="h-full w-full max-w-72 mx-auto flex flex-col justify-center items-center">
    <form id="kc-register-form" class="flex flex-col gap-4 w-full ${properties.kcFormClass!}"
          action="${url.registrationAction}"
          method="post">
        <div class="w-full flex flex-col justify-center items-start">
          <span class="text-white text-6xl font-bold mb-10 self-start">
            Happening now
          </span>
            <span class="text-white text-3xl font-bold mb-8 self-start">
            Join today.
          </span>
        </div>
        <@userProfileCommons.userProfileFormFields; callback, attribute>
            <#if callback = "afterField">
            <#-- render password fields just under the username or email (if used as username) -->
                <#if passwordRequired?? && (attribute.name == 'username' || (attribute.name == 'email' && realm.registrationEmailAsUsername))>
                    <div class="flex flex-col gap-4 w-full">
                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcInputWrapperClass!}">
                                <div class="${properties.kcInputGroup!}" dir="ltr">
                                    <div class="relative" dir="ltr">
                                        <input id="password" type="password"
                                               name="password"
                                               class="peer block w-full px-3 pt-5 pb-2 text-white bg-transparent border border-gray-600 rounded-md focus:outline-none focus:ring-0 focus:border-twitter-blue pr-12"
                                               placeholder=" "
                                               autocomplete="new-password"
                                               aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                                        >

                                        <label for="password" class="absolute text-gray-500 duration-300 transform -translate-y-4 scale-75 top-4 z-10 origin-[0] start-3
                                                peer-placeholder-shown:scale-100
                                                peer-placeholder-shown:translate-y-0
                                                peer-focus:scale-75
                                                peer-focus:-translate-y-4
                                                peer-focus:text-blue-500">
                                            Password
                                            <span class="text-red-300">*</span>
                                        </label>

                                        <button class="absolute top-1/2 right-3 transform -translate-y-1/2 bg-transparent p-2 ${properties.kcFormPasswordVisibilityButtonClass!}"
                                                type="button" aria-label="${msg('showPassword')}"
                                                aria-controls="password" data-password-toggle
                                                data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}"
                                                data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                                data-label-show="${msg('showPassword')}"
                                                data-label-hide="${msg('hidePassword')}">
                                            <i class="${properties.kcFormPasswordVisibilityIconShow!}"
                                               aria-hidden="true"></i>
                                        </button>
                                    </div>
                                </div>
                                <#if messagesPerField.existsError('password')>
                                    <span id="input-error-password"
                                          class="text-sm text-red-500 ${properties.kcInputErrorMessageClass!}"
                                          aria-live="polite">
		                                ${kcSanitize(messagesPerField.get('password'))?no_esc}
		                            </span>
                                </#if>
                            </div>
                        </div>

                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcInputWrapperClass!}">
                                <div class="${properties.kcInputGroup!}" dir="ltr">
                                    <div class="relative" dir="ltr">
                                        <input id="password-confirm" type="password"
                                               name="password-confirm"
                                               class="peer block w-full px-3 pt-5 pb-2 text-white bg-transparent border border-gray-600 rounded-md focus:outline-none focus:ring-0 focus:border-twitter-blue pr-12"
                                               placeholder=" "
                                               autocomplete="new-password"
                                               aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                                        >

                                        <label for="password-confirm" class="absolute text-gray-500 duration-300 transform -translate-y-4 scale-75 top-4 z-10 origin-[0] start-3
                                                peer-placeholder-shown:scale-100
                                                peer-placeholder-shown:translate-y-0
                                                peer-focus:scale-75
                                                peer-focus:-translate-y-4
                                                peer-focus:text-blue-500">
                                            Confirm password
                                            <span class="text-red-300">*</span>
                                        </label>

                                        <button class="absolute top-1/2 right-3 transform -translate-y-1/2 bg-transparent p-2 ${properties.kcFormPasswordVisibilityButtonClass!}"
                                                type="button"
                                                aria-label="${msg('showPassword')}"
                                                aria-controls="password-confirm" data-password-toggle
                                                data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}"
                                                data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                                data-label-show="${msg('showPassword')}"
                                                data-label-hide="${msg('hidePassword')}">
                                            <i class="${properties.kcFormPasswordVisibilityIconShow!}"
                                               aria-hidden="true"></i>
                                        </button>
                                    </div>
                                </div>

                                <#if messagesPerField.existsError('password-confirm')>
                                    <span id="input-error-password-confirm"
                                          class="text-sm text-red-500 ${properties.kcInputErrorMessageClass!}"
                                          aria-live="polite">
		                                ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
		                            </span>
                                </#if>
                            </div>
                        </div>
                    </div>
                </#if>
            </#if>
        </@userProfileCommons.userProfileFormFields>

        <#if recaptchaRequired?? && (recaptchaVisible!false)>
            <div class="form-group">
                <div class="${properties.kcInputWrapperClass!}">
                    <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"
                         data-action="${recaptchaAction}"></div>
                </div>
            </div>
        </#if>

        <div class="${properties.kcFormGroupClass!}">
            <#if recaptchaRequired?? && !(recaptchaVisible!false)>
                <script>
                    function onSubmitRecaptcha(token) {
                        document.getElementById("kc-register-form").requestSubmit();
                    }
                </script>
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} g-recaptcha"
                            data-sitekey="${recaptchaSiteKey}" data-callback='onSubmitRecaptcha'
                            data-action='${recaptchaAction}' type="submit">
                        ${msg("doRegister")}
                    </button>
                </div>
            <#else>
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="w-full relative overflow-hidden px-6 py-2 bg-blue-400 font-bold rounded-full group hover:cursor-pointer" ${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}
                    "
                    type="submit" value="${msg("doRegister")}"/>
                </div>
            </#if>
            <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                <div class="w-full max-w-72 mt-8 flex flex-col">
                    <span class="text-white font-bold self-start mb-4">Already have an account?</span>
                    <a href="${url.loginUrl}"
                       class="text-blue-400 text-center bg-transparent border border-gray-500 rounded-full py-1.5 font-semibold"
                    >
                        ${kcSanitize(msg("backToLogin"))?no_esc}
                    </a>
                </div>
            </div>
        </div>
    </form>
    <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
</div>
</body>
</html>