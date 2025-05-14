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
<body class="bg-gray-50">
    <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-lg">
            <!-- Logo -->
            <div class="flex justify-center">
                <img class="h-12 w-12" src="${url.resourcesPath}/img/x-logo.svg" alt="X Logo">
            </div>
            
            <!-- Tiêu đề -->
            <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
                Sign in to X
            </h2>

            <!-- Thông báo lỗi -->
            <#if message?has_content>
                <div class="rounded-md ${message.type == 'error'?then('bg-red-50 text-red-700', 'bg-green-50 text-green-700')} p-4">
                    ${kcSanitize(message.summary)?no_esc}
                </div>
            </#if>

            <!-- Form đăng nhập -->
            <form id="kc-form-login" class="mt-8 space-y-6" action="${url.loginAction}" method="post">
                <div class="rounded-md shadow-sm -space-y-px">
                    <div>
                        <label for="username" class="sr-only">Username</label>
                        <input id="username" name="username" type="text" required 
                            class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-twitter-blue focus:border-twitter-blue focus:z-10 sm:text-sm" 
                            placeholder="Phone, email, or username"
                            value="${(login.username!'')}"
                            autocomplete="username">
                    </div>
                    <div>
                        <label for="password" class="sr-only">Password</label>
                        <input id="password" name="password" type="password" required 
                            class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-twitter-blue focus:border-twitter-blue focus:z-10 sm:text-sm" 
                            placeholder="Password"
                            autocomplete="current-password">
                    </div>
                </div>

                <#if realm.resetPasswordAllowed>
                    <div class="flex items-center justify-end">
                        <div class="text-sm">
                            <a href="${url.loginResetCredentialsUrl}" class="font-medium text-twitter-blue hover:text-blue-500">
                                Forgot password?
                            </a>
                        </div>
                    </div>
                </#if>

                <div>
                    <button type="submit" 
                        class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-full text-white bg-twitter-blue hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-twitter-blue">
                        Sign in
                    </button>
                </div>
            </form>

            <!-- Social Login -->
            <#if social.providers?has_content>
                <div class="mt-6">
                    <div class="relative">
                        <div class="absolute inset-0 flex items-center">
                            <div class="w-full border-t border-gray-300"></div>
                        </div>
                        <div class="relative flex justify-center text-sm">
                            <span class="px-2 bg-white text-gray-500">
                                Or sign in with
                            </span>
                        </div>
                    </div>

                    <div class="mt-6 grid grid-cols-2 gap-3">
                        <#list social.providers as p>
                            <a href="${p.loginUrl}" 
                                class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">
                                <img class="h-5 w-5" src="${url.resourcesPath}/img/${p.alias}.svg" alt="${p.displayName}">
                                <span class="ml-2">${p.displayName}</span>
                            </a>
                        </#list>
                    </div>
                </div>
            </#if>

            <!-- Link đăng ký -->
            <div class="text-center mt-4">
                <span class="text-gray-600">Don't have an account?</span>
                <a href="${properties.registrationUrl!url.registrationUrl}" 
                   class="font-medium text-twitter-blue hover:text-blue-500 ml-1">
                    Sign up
                </a>
            </div>
        </div>
    </div>
</body>
</html>