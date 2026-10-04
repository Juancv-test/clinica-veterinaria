// Wrapper básico para fetch que intercepta el token y errores
const api = {
    baseUrl: 'http://localhost:8080/api',

    async request(endpoint, method = 'GET', body = null) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = { method, headers };
        if (body) {
            config.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(this.baseUrl + endpoint, config);
            
            // Si el token expiró o no hay sesión
            if (response.status === 401) {
                localStorage.clear();
                window.location.href = '/index.html';
                return null;
            }

            // Para 204 No Content
            if (response.status === 204) {
                return true;
            }

            const data = await response.json();

            // Manejo de errores controlados (400, 403, 404, 409)
            if (!response.ok) {
                let msj = data.mensajes ? data.mensajes.join('\n') : data.error;
                alert('Error: ' + msj);
                throw new Error(msj);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    get(endpoint) { return this.request(endpoint, 'GET'); },
    post(endpoint, body) { return this.request(endpoint, 'POST', body); },
    put(endpoint, body) { return this.request(endpoint, 'PUT', body); },
    patch(endpoint, body) { return this.request(endpoint, 'PATCH', body); },
    delete(endpoint) { return this.request(endpoint, 'DELETE'); }
};

// Función global para cerrar sesión
function logout() {
    localStorage.clear();
    window.location.href = '/index.html';
}
