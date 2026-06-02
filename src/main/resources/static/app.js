const state = {
    corretoras: [],
    validacoesCorretoras: {},
    acoes: [],
    usuarios: [],
    posicoes: [],
    operacoes: [],
    saldo: 0,
    usuarioId: localStorage.getItem("usuarioCarteiraId") || "",
    filtroCarteira: "",
    mediaAcaoA: "",
    mediaAcaoB: ""
};

const ACOES_EXEMPLO = [
    { ticker: "PETR4", mercado: "BR", nomeEmpresa: "Petrobras PN", moeda: "BRL", cotacaoAtual: 41.57 },
    { ticker: "VALE3", mercado: "BR", nomeEmpresa: "Vale ON", moeda: "BRL", cotacaoAtual: 58.42 },
    { ticker: "ITUB4", mercado: "BR", nomeEmpresa: "Itaú Unibanco PN", moeda: "BRL", cotacaoAtual: 34.20 },
    { ticker: "AAPL", mercado: "US", nomeEmpresa: "Apple", moeda: "USD", cotacaoAtual: 315.20 },
    { ticker: "MSFT", mercado: "US", nomeEmpresa: "Microsoft", moeda: "USD", cotacaoAtual: 460.36 },
    { ticker: "GOOGL", mercado: "US", nomeEmpresa: "Alphabet", moeda: "USD", cotacaoAtual: 172.88 }
];

const $ = (selector) => document.querySelector(selector);

const elements = {
    apiStatus: $("#apiStatus"),
    statusText: $("#statusText"),
    toast: $("#toast"),
    corretorasBody: $("#corretorasBody"),
    acoesBody: $("#acoesBody"),
    carteiraBody: $("#carteiraBody"),
    comprasBody: $("#comprasBody"),
    operacoesBody: $("#operacoesBody"),
    corretoraForm: $("#corretoraForm"),
    acaoForm: $("#acaoForm"),
    usuarioForm: $("#usuarioForm"),
    depositoForm: $("#depositoForm"),
    operacaoForm: $("#operacaoForm"),
    tipoOperacao: $("#tipoOperacao"),
    tipoOperacaoLabel: $("#tipoOperacaoLabel"),
    novaCompraOperacao: $("#novaCompraOperacao"),
    usuarioSelect: $("#usuarioSelect"),
    mediaAcaoA: $("#mediaAcaoA"),
    mediaAcaoB: $("#mediaAcaoB"),
    mediaDuasAcoesResultado: $("#mediaDuasAcoesResultado"),
    operacaoResumo: $("#operacaoResumo")
};

function setStatus(message, type = "ready") {
    elements.statusText.textContent = message;
    elements.apiStatus.className = "status-dot";
    if (type === "busy") elements.apiStatus.classList.add("busy");
    if (type === "error") elements.apiStatus.classList.add("error");
}

function showToast(message, type = "success") {
    elements.toast.textContent = message;
    elements.toast.className = `toast visible ${type === "error" ? "error" : ""}`;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        elements.toast.className = "toast";
    }, 4200);
}

async function requestJson(url, options = {}) {
    setStatus("Consultando API...", "busy");
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {
        const message =
            data?.mensagem ||
            data?.message ||
            data?.erro ||
            data?.error ||
            data ||
            "Não foi possível concluir a operação.";
        throw new Error(message);
    }

    setStatus("API respondeu com sucesso");
    return data;
}

function getFormData(form) {
    return Object.fromEntries(new FormData(form).entries());
}

function setTipoOperacao(tipo) {
    const normalized = tipo === "VENDA" ? "VENDA" : "COMPRA";
    elements.tipoOperacao.value = normalized;
    elements.tipoOperacaoLabel.value = normalized === "VENDA"
        ? "Venda pela compra"
        : "Compra";
    renderOperacaoResumo();
}

function formatMoney(value, currency = "BRL") {
    if (value === null || value === undefined || value === "") return "-";
    const locale = currency === "USD" ? "en-US" : "pt-BR";
    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency
    }).format(Number(value));
}

function formatAverageValue(value, moedaA, moedaB) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) return "-";
    if (moedaA === moedaB) return formatMoney(value, moedaA || "BRL");

    return Number(value).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatPercent(value) {
    if (value === null || value === undefined || value === "") return "-";
    return `${Number(value).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}%`;
}

function formatDate(value) {
    if (!value) return "-";
    const normalized = String(value).includes("T") ? value : String(value).replace(" ", "T");
    const date = new Date(normalized);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString("pt-BR");
}

function parseNumber(value) {
    return Number(String(value).replace(",", "."));
}

function signedClass(value) {
    const number = Number(value);
    if (number > 0) return "number-positive";
    if (number < 0) return "number-negative";
    return "";
}

function getAcaoById(id) {
    return state.acoes.find((acao) => String(acao.id) === String(id));
}

function getAcaoByTicker(ticker) {
    return state.acoes.find((acao) => acao.ticker === ticker);
}

function getPosicaoByTicker(ticker) {
    return state.posicoes.find((posicao) => posicao.ticker === ticker);
}

function getSelectedUsuario() {
    return state.usuarios.find((usuario) => String(usuario.id) === String(state.usuarioId));
}

function selectedUsuarioId() {
    return elements.usuarioSelect.value || state.usuarioId;
}

function pareceTickerAmericano(ticker) {
    return /^[A-Z]{1,5}$/.test(ticker);
}

function isTickerDuplicadoMessage(message) {
    return String(message).toLowerCase().includes("ticker") &&
        String(message).toLowerCase().includes("cadastrado");
}

function getOperacaoPreview() {
    const acaoId = elements.operacaoForm.elements.acaoId.value;
    const acao = getAcaoById(acaoId);
    const posicao = acao ? getPosicaoByTicker(acao.ticker) : null;
    const quantidade = parseNumber(elements.operacaoForm.elements.quantidade.value || 0);
    const precoUnitario = parseNumber(elements.operacaoForm.elements.precoUnitario.value || 0);
    const total = quantidade * precoUnitario;

    return {
        acao,
        posicao,
        quantidade,
        precoUnitario,
        total: Number.isFinite(total) ? total : 0
    };
}

function renderOperacaoResumo() {
    const { acao, posicao, quantidade, precoUnitario, total } = getOperacaoPreview();
    const tipo = elements.tipoOperacao.value;
    const saldoAposCompra = state.saldo - total;
    const moeda = acao?.moeda || "BRL";
    const cotacaoAtual = Number(acao?.cotacaoAtual || 0);
    const custoMedio = Number(posicao?.precoMedio || 0);
    const quantidadeDisponivel = Number(posicao?.quantidade || 0);
    const lucroBrutoCompra = (cotacaoAtual - precoUnitario) * quantidade;
    const lucroBrutoVenda = (precoUnitario - custoMedio) * quantidade;

    elements.operacaoResumo.className = "operation-summary";

    if (!acao) {
        elements.operacaoResumo.textContent = "Selecione uma ação para calcular a operação.";
        return;
    }

    if (tipo === "COMPRA" && total > state.saldo) {
        elements.operacaoResumo.classList.add("warning");
        elements.operacaoResumo.innerHTML = `
            Cotação atual da ação: <strong>${formatMoney(cotacaoAtual, moeda)}</strong><br>
            Total estimado: <strong>${formatMoney(total, moeda)}</strong><br>
            Lucro bruto estimado da compra: <strong class="${signedClass(lucroBrutoCompra)}">${formatMoney(lucroBrutoCompra, moeda)}</strong><br>
            Saldo insuficiente: <strong>${formatMoney(state.saldo, "BRL")}</strong>
        `;
        return;
    }

    if (tipo === "COMPRA") {
        elements.operacaoResumo.innerHTML = `
            Cotação atual da ação: <strong>${formatMoney(cotacaoAtual, moeda)}</strong><br>
            Total estimado: <strong>${formatMoney(total, moeda)}</strong><br>
            Lucro bruto estimado da compra: <strong class="${signedClass(lucroBrutoCompra)}">${formatMoney(lucroBrutoCompra, moeda)}</strong><br>
            Saldo após compra: <strong>${formatMoney(saldoAposCompra, "BRL")}</strong>
        `;
        return;
    }

    if (quantidade > quantidadeDisponivel) {
        elements.operacaoResumo.classList.add("warning");
        elements.operacaoResumo.innerHTML = `
            Quantidade disponível: <strong>${quantidadeDisponivel}</strong><br>
            Custo médio original: <strong>${formatMoney(custoMedio, moeda)}</strong><br>
            Cotação atual da ação: <strong>${formatMoney(cotacaoAtual, moeda)}</strong><br>
            Você não possui quantidade suficiente para vender ${quantidade} unidade(s).
        `;
        return;
    }

    elements.operacaoResumo.innerHTML = `
        Quantidade disponível: <strong>${quantidadeDisponivel}</strong><br>
        Custo médio original: <strong>${formatMoney(custoMedio, moeda)}</strong><br>
        Cotação atual da ação: <strong>${formatMoney(cotacaoAtual, moeda)}</strong><br>
        Total da venda: <strong>${formatMoney(total, moeda)}</strong><br>
        Lucro bruto estimado da venda: <strong class="${signedClass(lucroBrutoVenda)}">${formatMoney(lucroBrutoVenda, moeda)}</strong>
    `;
}

function sugerirMercadoDaAcao() {
    const ticker = elements.acaoForm.elements.ticker.value.trim().toUpperCase();
    if (ticker && pareceTickerAmericano(ticker)) {
        elements.acaoForm.elements.mercado.value = "US";
    }
}

function renderCorretoras(items) {
    if (!items.length) {
        elements.corretorasBody.innerHTML = `<tr><td class="empty-state" colspan="7">Nenhuma corretora encontrada.</td></tr>`;
        return;
    }

    elements.corretorasBody.innerHTML = items.map((item) => `
        ${(() => {
            const validacao = state.validacoesCorretoras[item.id];
            const motivo = validacao?.motivo || (item.validadaNaCvm ? "Validada por regra local" : "Pendente de validação");
            return `
        <tr>
            <td>${item.id ?? "-"}</td>
            <td>${item.cnpj ?? "-"}</td>
            <td>
                <strong>${item.nomeFantasia || item.razaoSocial || "-"}</strong><br>
                <span>${item.logradouro || ""}${item.numero ? `, ${item.numero}` : ""}</span>
            </td>
            <td>${item.cidade || "-"}${item.uf ? `/${item.uf}` : ""}</td>
            <td>${item.situacaoCadastral || "-"}</td>
            <td><span class="badge ${item.validadaNaCvm ? "ok" : "warn"}">${item.validadaNaCvm ? "Validada" : "Não validada"}</span></td>
            <td>${motivo}</td>
        </tr>
            `;
        })()}
    `).join("");
}

function renderAcoes(items) {
    renderAcaoOptions();

    if (!items.length) {
        elements.acoesBody.innerHTML = `<tr><td class="empty-state" colspan="7">Nenhuma ação encontrada.</td></tr>`;
        return;
    }

    elements.acoesBody.innerHTML = items.map((item) => `
        <tr>
            <td>${item.id ?? "-"}</td>
            <td><strong>${item.ticker ?? "-"}</strong></td>
            <td>${item.nomeEmpresa || "-"}</td>
            <td>${item.mercado || "-"}</td>
            <td>${item.moeda || "-"}</td>
            <td>${formatMoney(item.cotacaoAtual, item.moeda || "BRL")}</td>
            <td>${formatDate(item.dataHoraCotacao)}</td>
        </tr>
    `).join("");
}

function renderAcaoOptions() {
    const select = elements.operacaoForm?.elements.acaoId;
    if (!select) return;

    select.innerHTML = state.acoes.length
        ? state.acoes.map((acao) => `
            <option value="${acao.id}">${acao.ticker} - ${acao.nomeEmpresa || "Ação"} (#${acao.id})</option>
        `).join("")
        : `<option value="">Cadastre uma ação primeiro</option>`;
}

function renderUsuarios() {
    if (!state.usuarios.length) {
        elements.usuarioSelect.innerHTML = `<option value="">Crie um usuário</option>`;
        $("#usuarioCarteira").textContent = "-";
        return;
    }

    if (!state.usuarioId || !state.usuarios.some((usuario) => String(usuario.id) === String(state.usuarioId))) {
        state.usuarioId = String(state.usuarios[0].id);
        localStorage.setItem("usuarioCarteiraId", state.usuarioId);
    }

    elements.usuarioSelect.innerHTML = state.usuarios.map((usuario) => `
        <option value="${usuario.id}" ${String(usuario.id) === String(state.usuarioId) ? "selected" : ""}>
            ${usuario.nome || "Usuário"} (#${usuario.id})
        </option>
    `).join("");

    const usuario = getSelectedUsuario();
    $("#usuarioCarteira").textContent = usuario ? `${usuario.nome || "Usuário"} #${usuario.id}` : "-";
}

function renderCarteira() {
    const filteredRows = state.filtroCarteira
        ? state.posicoes.filter((row) => row.ticker?.toUpperCase().includes(state.filtroCarteira))
        : state.posicoes;

    const usuarioId = selectedUsuarioId();
    const comprasUsuario = state.operacoes.filter((operacao) =>
        operacao.tipo === "COMPRA" &&
        (!usuarioId || String(operacao.usuario?.id) === String(usuarioId))
    );
    const totalCompras = comprasUsuario.reduce((sum, operacao) => sum + Number(operacao.valorTotal || 0), 0);
    const totalAtual = state.posicoes.reduce((sum, row) => sum + Number(row.valorAtual || 0), 0);
    const resultado = state.posicoes.reduce((sum, row) => sum + Number(row.lucroPrejuizo || 0), 0);

    $("#saldoCarteira").textContent = formatMoney(state.saldo, "BRL");
    $("#totalComprasUsuario").textContent = formatMoney(totalCompras, "BRL");
    $("#totalAcoesCarteira").textContent = String(state.posicoes.length);
    $("#valorAtualCarteira").textContent = formatMoney(totalAtual, "BRL");
    $("#resultadoCarteira").textContent = formatMoney(resultado, "BRL");
    $("#resultadoCarteira").className = signedClass(resultado);
    renderOperacaoResumo();

    if (!filteredRows.length) {
        const message = state.filtroCarteira
            ? "Nenhuma ação da carteira corresponde ao filtro."
            : "Nenhuma ação na carteira.";
        elements.carteiraBody.innerHTML = `<tr><td class="empty-state" colspan="8">${message}</td></tr>`;
    } else {
        elements.carteiraBody.innerHTML = filteredRows.map((row) => {
            const acao = getAcaoByTicker(row.ticker);
            const moeda = acao?.moeda || "BRL";

            return `
                <tr>
                    <td><strong>${row.ticker}</strong><br><span>${row.nomeEmpresa || "-"}</span></td>
                    <td>${row.quantidade}</td>
                    <td>${formatMoney(row.precoMedio, moeda)}</td>
                    <td>${formatMoney(row.cotacaoAtual, moeda)}</td>
                    <td>${formatMoney(row.valorInvestido, moeda)}</td>
                    <td>${formatMoney(row.valorAtual, moeda)}</td>
                    <td class="${signedClass(row.lucroPrejuizo)}">${formatMoney(row.lucroPrejuizo, moeda)}</td>
                    <td class="${signedClass(row.rentabilidade)}">${formatPercent(row.rentabilidade)}</td>
                </tr>
            `;
        }).join("");
    }

    renderCompras();
    renderOperacoes();
    renderCustoMedio(filteredRows, state.posicoes);
    renderMediaDuasAcoes();
}

function renderCompras() {
    const usuarioId = selectedUsuarioId();
    const compras = state.operacoes
        .filter((operacao) => operacao.tipo === "COMPRA")
        .filter((operacao) => !usuarioId || String(operacao.usuario?.id) === String(usuarioId))
        .filter((operacao) => {
            const ticker = operacao.acao?.ticker || "";
            return !state.filtroCarteira || ticker.toUpperCase().includes(state.filtroCarteira);
        });

    if (!compras.length) {
        const message = state.filtroCarteira
            ? "Nenhuma compra corresponde ao filtro."
            : "Nenhuma compra cadastrada para este usuário.";
        elements.comprasBody.innerHTML = `<tr><td class="empty-state" colspan="9">${message}</td></tr>`;
        return;
    }

    elements.comprasBody.innerHTML = compras
        .sort((a, b) => String(b.dataOperacao || "").localeCompare(String(a.dataOperacao || "")))
        .map((operacao) => {
            const acao = getAcaoByTicker(operacao.acao?.ticker);
            const moeda = acao?.moeda || operacao.acao?.moeda || "BRL";
            const cotacaoAtual = Number(acao?.cotacaoAtual || operacao.acao?.cotacaoAtual || 0);
            const lucroBruto = (cotacaoAtual - Number(operacao.precoUnitario || 0)) * Number(operacao.quantidade || 0);
            const posicao = getPosicaoByTicker(operacao.acao?.ticker);
            const podeVender = Number(posicao?.quantidade || 0) > 0;

            return `
                <tr>
                    <td>${operacao.id ?? "-"}</td>
                    <td>${formatDate(operacao.dataOperacao)}</td>
                    <td><strong>${operacao.acao?.ticker || "-"}</strong></td>
                    <td>${operacao.quantidade ?? "-"}</td>
                    <td>${formatMoney(operacao.precoUnitario, moeda)}</td>
                    <td>${formatMoney(cotacaoAtual, moeda)}</td>
                    <td class="${signedClass(lucroBruto)}">${formatMoney(lucroBruto, moeda)}</td>
                    <td>${formatMoney(operacao.valorTotal, moeda)}</td>
                    <td>
                        <button class="table-action sell-action" type="button" data-sell-compra="${operacao.id}" ${podeVender ? "" : "disabled"}>
                            Vender
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
}

function prepararVendaDaCompra(compraId) {
    const compra = state.operacoes.find((operacao) => String(operacao.id) === String(compraId));
    const acao = getAcaoByTicker(compra?.acao?.ticker);
    const posicao = getPosicaoByTicker(compra?.acao?.ticker);

    if (!compra || !acao) {
        return showToast("Compra não encontrada para venda.", "error");
    }

    const quantidadeDisponivel = Number(posicao?.quantidade || 0);
    if (quantidadeDisponivel <= 0) {
        return showToast(`Você não possui ${compra.acao?.ticker} disponível para venda.`, "error");
    }

    const quantidadeVenda = Math.min(Number(compra.quantidade || 0), quantidadeDisponivel);

    elements.operacaoForm.elements.acaoId.value = String(acao.id);
    elements.operacaoForm.elements.quantidade.value = String(quantidadeVenda);
    elements.operacaoForm.elements.precoUnitario.value = Number(acao.cotacaoAtual || compra.precoUnitario || 0).toFixed(2);

    setTipoOperacao("VENDA");
    elements.operacaoForm.scrollIntoView({ behavior: "smooth", block: "center" });
    elements.operacaoForm.elements.quantidade.focus();
    showToast(`Venda de ${acao.ticker} preparada.`);
}

function renderOperacoes() {
    const usuarioId = selectedUsuarioId();
    const filtered = state.operacoes
        .filter((operacao) => !usuarioId || String(operacao.usuario?.id) === String(usuarioId))
        .filter((operacao) => {
            const ticker = operacao.acao?.ticker || "";
            return !state.filtroCarteira || ticker.toUpperCase().includes(state.filtroCarteira);
        });

    if (!filtered.length) {
        const message = state.filtroCarteira
            ? "Nenhuma operação corresponde ao filtro."
            : "Nenhuma operação cadastrada.";
        elements.operacoesBody.innerHTML = `<tr><td class="empty-state" colspan="8">${message}</td></tr>`;
        return;
    }

    elements.operacoesBody.innerHTML = filtered
        .sort((a, b) => String(b.dataOperacao || "").localeCompare(String(a.dataOperacao || "")))
        .map((operacao) => {
            const moeda = operacao.acao?.moeda || "BRL";

            return `
                <tr>
                    <td>${operacao.id ?? "-"}</td>
                    <td>${formatDate(operacao.dataOperacao)}</td>
                    <td><strong>${operacao.acao?.ticker || "-"}</strong></td>
                    <td>${operacao.usuario?.nome || "-"}${operacao.usuario?.id ? ` #${operacao.usuario.id}` : ""}</td>
                    <td><span class="badge ${operacao.tipo === "COMPRA" ? "ok" : "warn"}">${operacao.tipo}</span></td>
                    <td>${operacao.quantidade ?? "-"}</td>
                    <td>${formatMoney(operacao.precoUnitario, moeda)}</td>
                    <td>${formatMoney(operacao.valorTotal, moeda)}</td>
                </tr>
            `;
        }).join("");
}

function renderCustoMedio(filteredRows, allRows) {
    const target = state.filtroCarteira ? filteredRows[0] : allRows[0];

    if (!target) {
        $("#custoMedioResultado").textContent = "Selecione um usuário e registre operações para ver o custo médio.";
        return;
    }

    const moeda = getAcaoByTicker(target.ticker)?.moeda || "BRL";
    $("#custoMedioResultado").innerHTML = `
        <strong>${target.ticker}</strong><br>
        Quantidade em carteira: <strong>${target.quantidade}</strong><br>
        Custo médio: <strong>${formatMoney(target.precoMedio, moeda)}</strong><br>
        Lucro/prejuízo da ação: <strong class="${signedClass(target.lucroPrejuizo)}">${formatMoney(target.lucroPrejuizo, moeda)}</strong>
    `;
}

function renderMediaDuasAcoes() {
    const acoesOrdenadas = [...state.acoes].sort((a, b) => a.ticker.localeCompare(b.ticker));
    const options = acoesOrdenadas.map((acao) => {
        const posicao = getPosicaoByTicker(acao.ticker);
        const labelPosicao = posicao ? "na carteira" : "sem compra";
        return `<option value="${acao.ticker}">${acao.ticker} - ${acao.nomeEmpresa || "Ação"} (${labelPosicao})</option>`;
    }).join("");

    if (acoesOrdenadas.length < 2) {
        elements.mediaAcaoA.innerHTML = `<option value="">Cadastre duas ações</option>`;
        elements.mediaAcaoB.innerHTML = `<option value="">Cadastre duas ações</option>`;
        elements.mediaDuasAcoesResultado.textContent = "Cadastre pelo menos duas ações para comparar.";
        return;
    }

    if (!state.mediaAcaoA || !acoesOrdenadas.some((acao) => acao.ticker === state.mediaAcaoA)) {
        state.mediaAcaoA = acoesOrdenadas[0].ticker;
    }

    if (
        !state.mediaAcaoB ||
        state.mediaAcaoB === state.mediaAcaoA ||
        !acoesOrdenadas.some((acao) => acao.ticker === state.mediaAcaoB)
    ) {
        state.mediaAcaoB = acoesOrdenadas.find((acao) => acao.ticker !== state.mediaAcaoA)?.ticker || "";
    }

    elements.mediaAcaoA.innerHTML = options;
    elements.mediaAcaoB.innerHTML = options;
    elements.mediaAcaoA.value = state.mediaAcaoA;
    elements.mediaAcaoB.value = state.mediaAcaoB;

    const primeiraAcao = getAcaoByTicker(state.mediaAcaoA);
    const segundaAcao = getAcaoByTicker(state.mediaAcaoB);
    const primeiraPosicao = getPosicaoByTicker(state.mediaAcaoA);
    const segundaPosicao = getPosicaoByTicker(state.mediaAcaoB);

    if (!primeiraAcao || !segundaAcao || primeiraAcao.ticker === segundaAcao.ticker) {
        elements.mediaDuasAcoesResultado.textContent = "Selecione duas ações diferentes.";
        return;
    }

    const primeiraValor = Number(primeiraPosicao?.precoMedio ?? primeiraAcao.cotacaoAtual ?? 0);
    const segundaValor = Number(segundaPosicao?.precoMedio ?? segundaAcao.cotacaoAtual ?? 0);
    const primeiraFonte = primeiraPosicao ? "custo médio" : "cotação atual";
    const segundaFonte = segundaPosicao ? "custo médio" : "cotação atual";
    const moedasIguais = primeiraAcao.moeda === segundaAcao.moeda;
    const quantidadeTotal = Number(primeiraPosicao?.quantidade || 0) + Number(segundaPosicao?.quantidade || 0);
    const podeCalcularPonderada = primeiraPosicao && segundaPosicao && quantidadeTotal > 0;
    const mediaSimples = (primeiraValor + segundaValor) / 2;
    const mediaPonderada = podeCalcularPonderada
        ? (
            (primeiraValor * Number(primeiraPosicao.quantidade || 0)) +
            (segundaValor * Number(segundaPosicao.quantidade || 0))
        ) / quantidadeTotal
        : null;
    const avisoMoeda = moedasIguais
        ? ""
        : `<br><strong>Moedas diferentes (${primeiraAcao.moeda} e ${segundaAcao.moeda}): valores calculados sem conversão cambial.</strong>`;
    const avisoPonderada = podeCalcularPonderada
        ? `Média ponderada por quantidade: <strong>${formatAverageValue(mediaPonderada, primeiraAcao.moeda, segundaAcao.moeda)}</strong>`
        : "Média ponderada por quantidade: cadastre compras para as duas ações.";

    elements.mediaDuasAcoesResultado.innerHTML = `
        <strong>${primeiraAcao.ticker}</strong>: ${formatMoney(primeiraValor, primeiraAcao.moeda)}
        (${primeiraFonte}${primeiraPosicao ? `, ${primeiraPosicao.quantidade} unidades` : ""})<br>
        <strong>${segundaAcao.ticker}</strong>: ${formatMoney(segundaValor, segundaAcao.moeda)}
        (${segundaFonte}${segundaPosicao ? `, ${segundaPosicao.quantidade} unidades` : ""})<br>
        Média simples: <strong>${formatAverageValue(mediaSimples, primeiraAcao.moeda, segundaAcao.moeda)}</strong><br>
        ${avisoPonderada}
        ${avisoMoeda}
    `;
}

async function loadCorretoras() {
    try {
        state.corretoras = await requestJson("/corretoras");
        await loadValidacoesCorretoras(state.corretoras);
        renderCorretoras(state.corretoras);
    } catch (error) {
        setStatus("Erro ao listar corretoras", "error");
        showToast(error.message, "error");
    }
}

async function loadValidacoesCorretoras(corretoras) {
    const results = await Promise.allSettled(
        corretoras
            .filter((corretora) => corretora.id)
            .map(async (corretora) => {
                const validacao = await requestJson(`/corretoras/${encodeURIComponent(corretora.id)}/validacao`);
                return [corretora.id, validacao];
            })
    );

    results.forEach((result) => {
        if (result.status === "fulfilled") {
            const [id, validacao] = result.value;
            state.validacoesCorretoras[id] = validacao;
        }
    });
}

async function carregarCorretorasPadrao() {
    try {
        const criadas = await requestJson("/corretoras/padrao", {
            method: "POST"
        });
        showToast(criadas.length
            ? `${criadas.length} corretora(s) padrão cadastrada(s).`
            : "Lista padrão já estava cadastrada.");
        await loadCorretoras();
    } catch (error) {
        setStatus("Carga da lista padrão falhou", "error");
        showToast(error.message, "error");
    }
}

async function verCorretorasPadrao() {
    try {
        const padrao = await requestJson("/corretoras/padrao");
        elements.corretorasBody.innerHTML = padrao.map((item) => `
            <tr>
                <td>-</td>
                <td>${item.cnpj}</td>
                <td><strong>${item.nome}</strong><br><span>CEP ${item.cep}, ${item.numero}</span></td>
                <td>-</td>
                <td>Lista padrão</td>
                <td><span class="badge ok">Validada</span></td>
                <td>CNPJ incluído na lista padrão local</td>
            </tr>
        `).join("");
        showToast("Lista padrão exibida.");
    } catch (error) {
        setStatus("Consulta da lista padrão falhou", "error");
        showToast(error.message, "error");
    }
}

async function carregarAcoesExemplo() {
    try {
        if (!state.corretoras.length) {
            await loadCorretoras();
        }

        const corretora = state.corretoras[0];
        if (!corretora) {
            return showToast("Cadastre uma corretora ou carregue a lista padrão antes de criar ações de exemplo.", "error");
        }

        let cadastradas = 0;
        let jaExistentes = 0;
        const falhas = [];

        for (const exemplo of ACOES_EXEMPLO) {
            const payload = {
                ticker: exemplo.ticker,
                mercado: exemplo.mercado,
                corretoraId: corretora.id
            };

            try {
                const acao = await requestJson("/acoes", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                cadastradas += 1;
                state.acoes = [
                    ...state.acoes.filter((item) => item.id !== acao.id),
                    acao
                ];
            } catch (error) {
                if (isTickerDuplicadoMessage(error.message)) {
                    jaExistentes += 1;
                    continue;
                }
                falhas.push(`${exemplo.ticker}: ${error.message}`);
            }
        }

        await loadAcoes();

        if (falhas.length) {
            setStatus("Alguns exemplos falharam", "error");
            return showToast(`Criadas: ${cadastradas}. Já existentes: ${jaExistentes}. Falhas: ${falhas.join(" | ")}`, "error");
        }

        showToast(cadastradas
            ? `${cadastradas} ação(ões) de exemplo cadastrada(s). ${jaExistentes} já existia(m).`
            : "As ações de exemplo já estavam cadastradas.");
    } catch (error) {
        setStatus("Carga de ações de exemplo falhou", "error");
        showToast(error.message, "error");
    }
}

function verAcoesExemplo() {
    renderAcoes(ACOES_EXEMPLO.map((item) => ({
        ...item,
        id: "-",
        dataHoraCotacao: "Exemplo"
    })));
    showToast("Exemplos de ações exibidos.");
}

async function loadAcoes() {
    try {
        state.acoes = await requestJson("/acoes");
        renderAcoes(state.acoes);
        renderCarteira();
    } catch (error) {
        setStatus("Erro ao listar ações", "error");
        showToast(error.message, "error");
    }
}

async function loadUsuarios() {
    try {
        state.usuarios = await requestJson("/usuarios");
        renderUsuarios();
    } catch (error) {
        setStatus("Erro ao listar usuários", "error");
        showToast(error.message, "error");
    }
}

async function loadCarteira() {
    const usuarioId = selectedUsuarioId();

    if (!usuarioId) {
        state.posicoes = [];
        state.operacoes = [];
        state.saldo = 0;
        renderCarteira();
        return;
    }

    state.usuarioId = String(usuarioId);
    localStorage.setItem("usuarioCarteiraId", state.usuarioId);

    const [posicoesResult, operacoesResult, saldoResult] = await Promise.allSettled([
        requestJson(`/operacoes/carteira/${encodeURIComponent(usuarioId)}`),
        requestJson("/operacoes"),
        requestJson(`/financeiro/saldo/${encodeURIComponent(usuarioId)}`)
    ]);

    if (posicoesResult.status === "fulfilled") {
        state.posicoes = posicoesResult.value || [];
    } else {
        state.posicoes = [];
        showToast(posicoesResult.reason.message, "error");
    }

    if (operacoesResult.status === "fulfilled") {
        state.operacoes = operacoesResult.value || [];
    } else {
        state.operacoes = [];
        showToast(operacoesResult.reason.message, "error");
    }

    if (saldoResult.status === "fulfilled") {
        state.saldo = Number(saldoResult.value || 0);
    } else {
        state.saldo = 0;
        showToast(saldoResult.reason.message, "error");
    }

    renderUsuarios();
    renderCarteira();

    if ([posicoesResult, operacoesResult, saldoResult].some((result) => result.status === "rejected")) {
        setStatus("Carteira carregada parcialmente", "error");
    }
}

async function submitCorretora(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = getFormData(form);

    try {
        const corretora = await requestJson("/corretoras", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        form.reset();
        state.corretoras = [
            ...state.corretoras.filter((item) => item.id !== corretora.id),
            corretora
        ];
        renderCorretoras(state.corretoras);
        showToast("Corretora cadastrada.");
        await loadCorretoras();
    } catch (error) {
        setStatus("Cadastro de corretora falhou", "error");
        showToast(error.message, "error");
    }
}

async function submitAcao(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = getFormData(form);
    payload.ticker = payload.ticker.toUpperCase();
    if (payload.mercado === "BR" && pareceTickerAmericano(payload.ticker)) {
        payload.mercado = "US";
        form.elements.mercado.value = "US";
        showToast(`Mercado alterado para US para cadastrar ${payload.ticker}.`);
    }
    payload.corretoraId = Number(payload.corretoraId);

    try {
        const acao = await requestJson("/acoes", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        form.reset();
        state.acoes = [
            ...state.acoes.filter((item) => item.id !== acao.id),
            acao
        ];
        renderAcoes(state.acoes);
        showToast("Ação cadastrada.");
        await loadAcoes();
    } catch (error) {
        if (isTickerDuplicadoMessage(error.message)) {
            const acaoExistente = await requestJson(`/acoes/ticker/${encodeURIComponent(payload.ticker)}`);
            state.acoes = [
                ...state.acoes.filter((item) => item.id !== acaoExistente.id),
                acaoExistente
            ];
            renderAcoes(state.acoes);
            showToast(`${payload.ticker} já estava cadastrado. Lista atualizada.`);
            await loadAcoes();
            return;
        }
        setStatus("Cadastro de ação falhou", "error");
        showToast(error.message, "error");
    }
}

async function submitUsuario(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = getFormData(form);

    try {
        const usuario = await requestJson("/usuarios", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        form.reset();
        state.usuarioId = String(usuario.id);
        state.usuarios = [
            ...state.usuarios.filter((item) => item.id !== usuario.id),
            usuario
        ];
        localStorage.setItem("usuarioCarteiraId", state.usuarioId);
        renderUsuarios();
        showToast("Usuário criado.");
        await loadUsuarios();
        await loadCarteira();
    } catch (error) {
        setStatus("Cadastro de usuário falhou", "error");
        showToast(error.message, "error");
    }
}

async function submitDeposito(event) {
    event.preventDefault();
    const usuarioId = selectedUsuarioId();
    if (!usuarioId) return showToast("Crie ou selecione um usuário.", "error");

    const form = event.currentTarget;
    const payload = getFormData(form);
    payload.valor = parseNumber(payload.valor);

    try {
        await requestJson(`/financeiro/deposito/${encodeURIComponent(usuarioId)}`, {
            method: "POST",
            body: JSON.stringify(payload)
        });
        form.reset();
        showToast("Depósito registrado.");
        await loadCarteira();
    } catch (error) {
        setStatus("Depósito falhou", "error");
        showToast(error.message, "error");
    }
}

async function submitOperacao(event) {
    event.preventDefault();
    const usuarioId = selectedUsuarioId();
    if (!usuarioId) return showToast("Crie ou selecione um usuário.", "error");

    const form = event.currentTarget;
    const payload = getFormData(form);
    payload.usuarioId = Number(usuarioId);
    payload.acaoId = Number(payload.acaoId);
    payload.quantidade = Number(payload.quantidade);
    payload.precoUnitario = parseNumber(payload.precoUnitario);

    if (!payload.acaoId) return showToast("Cadastre e selecione uma ação.", "error");

    const totalOperacao = payload.quantidade * payload.precoUnitario;
    if (payload.tipo === "COMPRA" && totalOperacao > state.saldo) {
        renderOperacaoResumo();
        return showToast(
            `Saldo insuficiente. Total da compra: ${formatMoney(totalOperacao, "BRL")}. Saldo atual: ${formatMoney(state.saldo, "BRL")}.`,
            "error"
        );
    }

    if (payload.tipo === "VENDA") {
        const acao = getAcaoById(payload.acaoId);
        const posicao = acao ? getPosicaoByTicker(acao.ticker) : null;
        const quantidadeDisponivel = Number(posicao?.quantidade || 0);

        if (payload.quantidade > quantidadeDisponivel) {
            renderOperacaoResumo();
            return showToast(
                `Quantidade insuficiente. Disponível para venda: ${quantidadeDisponivel}.`,
                "error"
            );
        }
    }

    try {
        const operacao = await requestJson("/operacoes", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        form.reset();
        setTipoOperacao("COMPRA");
        renderAcaoOptions();
        state.operacoes = [
            ...state.operacoes.filter((item) => item.id !== operacao.id),
            operacao
        ];
        renderCarteira();
        showToast("Operação registrada.");
        await loadCarteira();
    } catch (error) {
        setStatus("Cadastro de operação falhou", "error");
        showToast(error.message, "error");
    }
}

async function buscarCorretoraPorCnpj() {
    const cnpj = $("#buscarCnpj").value.trim();
    if (!cnpj) return showToast("Informe um CNPJ para buscar.", "error");

    try {
        const corretora = await requestJson(`/corretoras/cnpj/${encodeURIComponent(cnpj)}`);
        await loadValidacoesCorretoras([corretora]);
        renderCorretoras([corretora]);
    } catch (error) {
        setStatus("Busca por CNPJ falhou", "error");
        showToast(error.message, "error");
    }
}

async function buscarCorretoraPorId() {
    const id = $("#buscarCorretoraId").value.trim();
    if (!id) return showToast("Informe um ID de corretora.", "error");

    try {
        const corretora = await requestJson(`/corretoras/${encodeURIComponent(id)}`);
        await loadValidacoesCorretoras([corretora]);
        renderCorretoras([corretora]);
    } catch (error) {
        setStatus("Busca por ID falhou", "error");
        showToast(error.message, "error");
    }
}

async function buscarAcaoPorTicker() {
    const ticker = $("#buscarTicker").value.trim();
    if (!ticker) return showToast("Informe um ticker para buscar.", "error");

    try {
        const acao = await requestJson(`/acoes/ticker/${encodeURIComponent(ticker.toUpperCase())}`);
        renderAcoes([acao]);
    } catch (error) {
        setStatus("Busca por ticker falhou", "error");
        showToast(error.message, "error");
    }
}

async function atualizarCotacao() {
    const id = $("#acaoIdCotacao").value.trim();
    if (!id) return showToast("Informe o ID da ação.", "error");

    try {
        const acao = await requestJson(`/acoes/${encodeURIComponent(id)}/atualizar-cotacao`, {
            method: "PUT"
        });
        showToast(`Cotação de ${acao.ticker} atualizada.`);
        await loadAcoes();
        await loadCarteira();
    } catch (error) {
        setStatus("Atualização de cotação falhou", "error");
        showToast(error.message, "error");
    }
}

function setupTabs() {
    document.querySelectorAll(".tab-button").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".tab-button").forEach((item) => item.classList.remove("active"));
            document.querySelectorAll(".panel").forEach((panel) => panel.classList.remove("active"));
            button.classList.add("active");
            $(`#${button.dataset.tab}`).classList.add("active");
        });
    });
}

function bindEvents() {
    setupTabs();
    elements.corretoraForm.addEventListener("submit", submitCorretora);
    elements.acaoForm.addEventListener("submit", submitAcao);
    elements.acaoForm.elements.ticker.addEventListener("input", sugerirMercadoDaAcao);
    elements.usuarioForm.addEventListener("submit", submitUsuario);
    elements.depositoForm.addEventListener("submit", submitDeposito);
    elements.operacaoForm.addEventListener("submit", submitOperacao);
    elements.comprasBody.addEventListener("click", (event) => {
        const button = event.target.closest("[data-sell-compra]");
        if (!button) return;
        prepararVendaDaCompra(button.dataset.sellCompra);
    });
    elements.novaCompraOperacao.addEventListener("click", () => {
        setTipoOperacao("COMPRA");
        renderOperacaoResumo();
    });
    elements.operacaoForm.elements.acaoId.addEventListener("change", renderOperacaoResumo);
    elements.operacaoForm.elements.quantidade.addEventListener("input", renderOperacaoResumo);
    elements.operacaoForm.elements.precoUnitario.addEventListener("input", renderOperacaoResumo);
    elements.usuarioSelect.addEventListener("change", loadCarteira);
    elements.mediaAcaoA.addEventListener("change", () => {
        state.mediaAcaoA = elements.mediaAcaoA.value;
        renderMediaDuasAcoes();
    });
    elements.mediaAcaoB.addEventListener("change", () => {
        state.mediaAcaoB = elements.mediaAcaoB.value;
        renderMediaDuasAcoes();
    });
    $("#refreshCorretoras").addEventListener("click", loadCorretoras);
    $("#carregarCorretorasPadrao").addEventListener("click", carregarCorretorasPadrao);
    $("#verCorretorasPadrao").addEventListener("click", verCorretorasPadrao);
    $("#carregarAcoesExemplo").addEventListener("click", carregarAcoesExemplo);
    $("#verAcoesExemplo").addEventListener("click", verAcoesExemplo);
    $("#refreshAcoes").addEventListener("click", loadAcoes);
    $("#refreshCarteira").addEventListener("click", loadCarteira);
    $("#buscarCorretoraCnpj").addEventListener("click", buscarCorretoraPorCnpj);
    $("#buscarCorretoraIdBtn").addEventListener("click", buscarCorretoraPorId);
    $("#buscarAcaoTicker").addEventListener("click", buscarAcaoPorTicker);
    $("#atualizarCotacao").addEventListener("click", atualizarCotacao);
    $("#limparBuscaCorretora").addEventListener("click", loadCorretoras);
    $("#limparBuscaAcao").addEventListener("click", loadAcoes);
    $("#filtrarCarteira").addEventListener("click", () => {
        state.filtroCarteira = $("#filtroCarteiraTicker").value.trim().toUpperCase();
        renderCarteira();
    });
    $("#mostrarCarteiraToda").addEventListener("click", () => {
        state.filtroCarteira = "";
        $("#filtroCarteiraTicker").value = "";
        renderCarteira();
    });
}

async function init() {
    bindEvents();
    await Promise.all([loadCorretoras(), loadAcoes(), loadUsuarios()]);
    await loadCarteira();
}

init();
