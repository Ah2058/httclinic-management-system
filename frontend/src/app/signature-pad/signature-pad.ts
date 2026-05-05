import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  afterNextRender,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { fromEvent } from 'rxjs';

@Component({
  selector: 'app-signature-pad',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './signature-pad.html',
  styleUrl: './signature-pad.css',
  host: {
    class: 'sig-host'
  }
})
export class SignaturePadComponent {
  readonly dataUrl = input<string>('');
  readonly dataUrlChange = output<string>();

  private readonly destroyRef = inject(DestroyRef);
  private readonly canvasRef =
    viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  private readonly isDrawing = signal(false);
  private readonly canvasReady = signal(false);
  private lastPoint: { x: number; y: number } | null = null;

  protected readonly isEmpty = computed(() => !this.dataUrl());

  constructor() {
    afterNextRender(() => {
      this.setupCanvas();
      this.bindResize();
      this.canvasReady.set(true);
    });

    effect(() => {
      if (!this.canvasReady()) return;
      if (this.isDrawing()) return;
      this.applyExternalDataUrl(this.dataUrl());
    });
  }

  protected onClear(): void {
    this.clearCanvas();
    this.dataUrlChange.emit('');
  }

  protected onPointerDown(ev: PointerEvent): void {
    const canvas = this.canvasRef().nativeElement;
    canvas.setPointerCapture(ev.pointerId);
    this.isDrawing.set(true);
    this.lastPoint = this.toCanvasPoint(ev);
  }

  protected onPointerMove(ev: PointerEvent): void {
    if (!this.isDrawing()) return;
    const ctx = this.ctx();
    if (!ctx) return;

    const next = this.toCanvasPoint(ev);
    const prev = this.lastPoint ?? next;

    ctx.beginPath();
    ctx.moveTo(prev.x, prev.y);
    ctx.lineTo(next.x, next.y);
    ctx.stroke();

    this.lastPoint = next;
  }

  protected onPointerUpOrCancel(): void {
    if (!this.isDrawing()) return;
    this.isDrawing.set(false);
    this.lastPoint = null;
    this.emitDataUrl();
  }

  private bindResize(): void {
    fromEvent(window, 'resize')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        const before = this.snapshotImage();
        this.resizeToContainer();
        this.drawImage(before);
      });
  }

  private setupCanvas(): void {
    this.resizeToContainer();
    const ctx = this.ctx();
    if (!ctx) return;

    ctx.lineWidth = 2.25;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = '#0b1220';
  }

  private applyExternalDataUrl(url: string): void {
    if (!url) {
      this.clearCanvas();
      return;
    }
    const img = new Image();
    img.onload = () => {
      this.clearCanvas();
      this.drawImage(img);
    };
    img.src = url;
  }

  private ctx(): CanvasRenderingContext2D | null {
    return this.canvasRef().nativeElement.getContext('2d');
  }

  private toCanvasPoint(ev: PointerEvent): { x: number; y: number } {
    const canvas = this.canvasRef().nativeElement;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    return {
      x: (ev.clientX - rect.left) * scaleX,
      y: (ev.clientY - rect.top) * scaleY
    };
  }

  private resizeToContainer(): void {
    const canvas = this.canvasRef().nativeElement;
    const container = canvas.parentElement;
    if (!container) return;

    const rect = container.getBoundingClientRect();
    const dpr = Math.max(1, Math.floor(window.devicePixelRatio || 1));

    const width = Math.max(320, Math.floor(rect.width));
    const height = 180;

    canvas.width = Math.floor(width * dpr);
    canvas.height = Math.floor(height * dpr);
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    const ctx = this.ctx();
    if (!ctx) return;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    // Background
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);
    ctx.fillStyle = '#000000';
  }

  private clearCanvas(): void {
    const canvas = this.canvasRef().nativeElement;
    const ctx = this.ctx();
    if (!ctx) return;

    const rect = canvas.getBoundingClientRect();
    ctx.clearRect(0, 0, rect.width, rect.height);
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, rect.width, rect.height);
    ctx.fillStyle = '#000000';
  }

  private snapshotImage(): HTMLImageElement | null {
    const url = this.canvasRef().nativeElement.toDataURL('image/png');
    if (!url) return null;
    const img = new Image();
    img.src = url;
    return img;
  }

  private drawImage(img: HTMLImageElement | null): void {
    if (!img) return;
    const ctx = this.ctx();
    if (!ctx) return;
    const canvas = this.canvasRef().nativeElement;
    const rect = canvas.getBoundingClientRect();
    ctx.drawImage(img, 0, 0, rect.width, rect.height);
  }

  private emitDataUrl(): void {
    try {
      const canvas = this.canvasRef().nativeElement;
      const url = canvas.toDataURL('image/png');
      this.dataUrlChange.emit(url);
    } catch {
      // Some browsers may block data URLs in restrictive modes.
    }
  }
}
