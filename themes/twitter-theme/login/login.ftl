<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign in to X</title>
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
<body class="h-screen w-screen bg-black">
<div class="h-full w-full max-w-sm mx-auto flex flex-col justify-center items-center">

    <!-- Error notification -->
    <#if message?has_content>
        <div class="rounded-md ${message.type == 'error'?then('bg-red-50 text-red-700', 'bg-green-50 text-green-700')} p-4">
            ${kcSanitize(message.summary)?no_esc}
        </div>
    </#if>
    <div class="w-full flex flex-col justify-center items-center">
        <div class="w-full flex justify-start items-center mb-8">
            <span class="text-white text-4xl font-bold">Sign in to </span>
            <svg viewBox="0 0 24 24" aria-hidden="true" class="h-9 w-9 text-white fill-current ml-2">
                <g>
                    <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"></path>
                </g>
            </svg>
        </div>


        <!-- Social login -->
        <#if social.providers?has_content>
            <div class="w-full max-w-sm mb-5">
                <div class="flex flex-col gap-3 mb-5">
                    <#list social.providers as p>
                        <a href="${p.loginUrl}"
                           class="w-full relative overflow-hidden inline-flex justify-center py-2 px-4 border border-gray-300 rounded-full bg-white text-sm font-medium text-gray-800 group">
                            <img class="h-5 w-5 relative z-10" src="${url.resourcesPath}/img/${p.alias}.svg" alt="${p.displayName}">
                            <span class="ml-2 relative z-10">Sign in with ${p.displayName}</span>
                            <span class="absolute inset-0 bg-black opacity-0 group-hover:opacity-5 transition-opacity duration-100 ease-in-out"
                                  aria-hidden="true"
                            ></span>
                        </a>
                    </#list>
                </div>
                <div class="flex items-center">
                    <div class="flex-grow border-t border-gray-700"></div>
                    <span class="mx-2 font-semibold text-gray-300">or</span>
                    <div class="flex-grow border-t border-gray-700"></div>
                </div>
            </div>
        </#if>

        <!-- Default login -->
        <div class="w-full flex flex-col mb-7">
            <form id="kc-form-login" class="w-full flex flex-col gap-2" action="${url.loginAction}"
                  method="post">
                <div class="relative">
                    <input id="username" name="username" type="text" required
                           class="peer block w-full px-3 pt-5 pb-2 text-white bg-transparent border border-gray-600 rounded-md focus:outline-none focus:ring-0 focus:border-twitter-blue"
                           placeholder=" "
                           value="${(login.username!'')}"
                           autocomplete="username">
                    <label for="username"
                           class="absolute text-gray-500 duration-300 transform -translate-y-4 scale-75 top-4 z-10 origin-[0] start-3
                                    peer-placeholder-shown:scale-100
                                    peer-placeholder-shown:translate-y-0
                                    peer-focus:scale-75
                                    peer-focus:-translate-y-4
                                    peer-focus:text-blue-500">
                        Email or username
                    </label>
                </div>
                <div class="relative">
                    <input id="password" name="password" type="password" required
                           class="peer block w-full px-3 pt-5 pb-2 text-white bg-transparent border border-gray-600 rounded-md focus:outline-none focus:ring-0 focus:border-twitter-blue"
                           placeholder=" "
                           autocomplete="current-password">
                    <label for="password" class="absolute text-gray-500 duration-300 transform -translate-y-4 scale-75 top-4 z-10 origin-[0] start-3
                                    peer-placeholder-shown:scale-100
                                    peer-placeholder-shown:translate-y-0
                                    peer-focus:scale-75
                                    peer-focus:-translate-y-4
                                    peer-focus:text-blue-500">
                        Password
                    </label>
                </div>
                <div class="mt-3">
                    <button type="submit"
                            class="w-full relative overflow-hidden px-6 py-2 bg-white font-bold rounded-full group"
                    >
                        <span class="relative z-10 text-sm">Login in</span>
                        <span class="absolute inset-0 bg-black opacity-0 group-hover:opacity-10 transition-opacity duration-100 ease-in-out"
                              aria-hidden="true"
                        ></span>
                    </button>
                </div>
            </form>
        </div>

        <!-- Reset password -->
        <#if !realm.resetPasswordAllowed>
            <div class="w-full text-sm">
                <button type="button"
                        class="w-full relative overflow-hidden px-6 py-2 bg-black border border-gray-500 text-white font-bold rounded-full group"
                >
                    <a href="${url.loginResetCredentialsUrl}" class="relative z-10">
                        Forgot password?
                    </a>
                    <span class="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-100 ease-in-out"
                          aria-hidden="true"
                    ></span>
                </button>
            </div>
        </#if>

        <!-- Sign up -->
        <div class="text-center mt-8">
            <span class="text-gray-400 text-sm">Don't have an account?</span>
            <a href="${properties.registrationUrl!url.registrationUrl}"
               class="text-twitter-blue hover:underline ml-1">
                Sign up
            </a>
        </div>
    </div>
</div>
</body>
</html>