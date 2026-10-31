import { useEffect, useMemo, useState } from 'react'
import { Activity, AlertTriangle, ArrowUpRight, BadgeCheck, Building2, Calculator, CalendarDays, FileText, LayoutDashboard, RefreshCw, Search, Truck } from 'lucide-react'

type Order = {
  orderNumber: string; supplierName: string; promisedDate: string; completionDate: string
  orderValue: number; orderedQuantity: number; acceptedQuantity: number
  status: string; penalty: number; explanation: string
}
type Supplier = {
  supplierName: string; totalOrders: number; compliantOrders: number; failedOrders: number
  otifPercentage: number; monthlyOrderValue: number; recommendedPenalty: number; thresholdBreached: boolean
}
type Report = {
  month: string; totalOrders: number; compliantOrders: number; failedOrders: number
  forceMajeureApprovedOrders: number; recommendedPenalties: number; suppliers: Supplier[]; orders: Order[]
}

const money = (n: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n)

export default function App() {
  const [report, setReport] = useState<Report | null>(null)
  const [month, setMonth] = useState('2026-10')
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<Order | null>(null)
  const [error, setError] = useState('')

  async function load() {
    try {
      setError('')
      const res = await fetch(`/api/v1/demo/monthly-report?month=${month}`)
      if (!res.ok) throw new Error('Backend returned an error')
      setReport(await res.json())
    } catch {
      setError('Cannot reach the backend. Start Spring Boot on port 8080 and refresh.')
    }
  }
  useEffect(() => { load() }, [month])

  const filtered = useMemo(() => (report?.orders ?? []).filter(o =>
    `${o.orderNumber} ${o.supplierName} ${o.status}`.toLowerCase().includes(query.toLowerCase())
  ), [report, query])

  return <div className="shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-icon"><Activity size={20}/></div><div><b>Supplier SLA</b><small>Compliance platform</small></div></div>
      <div className="nav-label">WORKSPACE</div>
      <div className="nav active"><LayoutDashboard size={17}/> Overview</div>
      <div className="nav"><Building2 size={17}/> Suppliers</div>
      <div className="nav"><FileText size={17}/> Agreements</div>
      <div className="nav"><Truck size={17}/> Deliveries & GRNs</div>
      <div className="nav"><CalendarDays size={17}/> Exceptions</div>
      <div className="nav"><Calculator size={17}/> Evaluations</div>
      <div className="nav"><BadgeCheck size={17}/> Penalty approvals</div>
      <div className="sidebar-bottom"><span className="online-dot"/> Demo environment <small>v0.1.0</small></div>
    </aside>
    <main className="main">
      <header className="topbar">
        <div><div className="eyebrow">PROCUREMENT / PERFORMANCE</div><h1>Supplier compliance</h1><p>Monthly delivery performance and explainable penalty assessments.</p></div>
        <div className="toolbar"><input type="month" value={month} onChange={e => setMonth(e.target.value)}/><button className="icon-button" onClick={load} title="Refresh"><RefreshCw size={17}/></button><div className="avatar">PM</div></div>
      </header>
      {error && <div className="error"><AlertTriangle size={18}/>{error}</div>}
      {report && <>
        <section className="metric-grid">
          <Metric title="Orders evaluated" value={report.totalOrders.toString()} hint="Promised-date cohort" icon={<FileText/>}/>
          <Metric title="OTIF compliant" value={report.compliantOrders.toString()} hint={`${(report.compliantOrders / report.totalOrders * 100).toFixed(1)}% of orders`} icon={<BadgeCheck/>}/>
          <Metric title="Failed orders" value={report.failedOrders.toString()} hint="Requires assessment" icon={<AlertTriangle/>}/>
          <Metric title="Recommended penalties" value={money(report.recommendedPenalties)} hint="Pending approval" icon={<Calculator/>} emphasis/>
        </section>
        <section className="section">
          <div className="section-head"><div><h2>Supplier performance</h2><p>Monthly OTIF target: 95% · Exactly 95% meets the target</p></div><span className="tag">OCTOBER COHORT</span></div>
          <div className="supplier-grid">{report.suppliers.map(s => <div className="supplier-card" key={s.supplierName}>
            <div className="supplier-top"><div className="supplier-avatar">{s.supplierName.slice(-1)}</div><span className="status-bad">{s.thresholdBreached ? 'Below target' : 'On target'}</span></div>
            <b>{s.supplierName}</b><div className="score-row"><strong>{s.otifPercentage.toFixed(2)}%</strong><span>OTIF</span></div>
            <div className="progress"><div style={{ width: `${Math.min(100, s.otifPercentage)}%` }}/></div>
            <div className="supplier-foot"><span>{s.compliantOrders}/{s.totalOrders} compliant</span><span>{money(s.recommendedPenalty)}</span></div>
          </div>)}</div>
        </section>
        <section className="section">
          <div className="section-head"><div><h2>Order-level assessments</h2><p>Open an order to inspect the evidence and decision rationale.</p></div>
            <div className="search"><Search size={16}/><input placeholder="Search orders or suppliers" value={query} onChange={e => setQuery(e.target.value)}/></div>
          </div>
          <div className="table-wrap"><table><thead><tr><th>Purchase order</th><th>Supplier</th><th>Promised date</th><th>Completion</th><th>Accepted</th><th>Decision</th><th>Penalty</th><th></th></tr></thead>
            <tbody>{filtered.map(o => <tr key={o.orderNumber} onClick={() => setSelected(o)}>
              <td><b>{o.orderNumber}</b></td><td>{o.supplierName}</td><td>{o.promisedDate}</td><td>{o.completionDate}</td><td>{o.acceptedQuantity.toLocaleString()} / {o.orderedQuantity.toLocaleString()}</td>
              <td><span className={`pill ${o.status === 'COMPLIANT' ? 'good' : 'bad'}`}>{o.status.replace(/ /g, "-")}</span></td><td>{o.penalty ? money(o.penalty) : '—'}</td><td><ArrowUpRight size={16}/></td>
            </tr>)}</tbody></table></div>
        </section>
        <section className="note"><div className="note-icon"><AlertTriangle size={18}/></div><div><b>Decision governance</b><p>These are illustrative demo assessments. Production penalties require the applicable signed agreement, verified receipt evidence, approved exceptions and procurement/finance review.</p></div></section>
      </>}
    </main>
    {selected && <div className="modal-backdrop" onClick={() => setSelected(null)}><div className="modal" onClick={e => e.stopPropagation()}>
      <div className="modal-head"><div><span className="eyebrow">ORDER EXPLANATION</span><h2>{selected.orderNumber}</h2></div><button className="close" onClick={() => setSelected(null)}>×</button></div>
      <div className="detail-grid"><div><small>Supplier</small><b>{selected.supplierName}</b></div><div><small>Decision</small><b>{selected.status.replace(/ /g, "-")}</b></div><div><small>Promised date</small><b>{selected.promisedDate}</b></div><div><small>Completion date</small><b>{selected.completionDate}</b></div><div><small>Order value</small><b>{money(selected.orderValue)}</b></div><div><small>Penalty (demo)</small><b>{money(selected.penalty)}</b></div></div>
      <div className="explanation"><b>Why this decision?</b><p>{selected.explanation}</p></div>
      <button className="primary full" onClick={() => setSelected(null)}>Close explanation</button>
    </div></div>}
  </div>
}

function Metric({ title, value, hint, icon, emphasis = false }: {title:string;value:string;hint:string;icon:React.ReactNode;emphasis?:boolean}) {
  return <div className={`metric ${emphasis ? 'metric-emphasis' : ''}`}><div className="metric-top"><span>{title}</span><div className="metric-icon">{icon}</div></div><strong>{value}</strong><small>{hint}</small></div>
}
