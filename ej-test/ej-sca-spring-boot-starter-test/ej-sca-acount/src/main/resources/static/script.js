// 密码可见性切换
const togglePassword = document.getElementById('togglePassword');
const password = document.getElementById('password');

togglePassword.addEventListener('click', function () {
    // 切换密码可见性
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);

    // 切换图标
    this.querySelector('i').classList.toggle('fa-eye');
    this.querySelector('i').classList.toggle('fa-eye-slash');
});

// 登录表单提交
const loginForm = document.getElementById('loginForm');
loginForm.addEventListener('submit', function (e) {
    e.preventDefault();

    // 获取表单数据
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // 简单验证（实际应用中应使用更完善的验证）
    if (!username || !password) {
        alert('请输入用户名和密码');
        return;
    }

    // 模拟登录过程
    const submitButton = this.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fa fa-spinner fa-spin mr-2"></i> 登录中...';

    // 模拟API调用（实际应用中应替换为真实的API调用）
    setTimeout(() => {
        // 模拟登录成功
        alert('登录成功！即将跳转到主页...');
        submitButton.innerHTML = '登录';
        submitButton.disabled = false;

        // 实际应用中这里应该是页面跳转
        // window.location.href = '/dashboard';
    }, 1500);
});

// 添加输入框动画效果
const inputs = document.querySelectorAll('input');
inputs.forEach(input => {
    input.addEventListener('focus', () => {
        input.parentElement.classList.add('scale-105');
    });

    input.addEventListener('blur', () => {
        input.parentElement.classList.remove('scale-105');
    });
});
