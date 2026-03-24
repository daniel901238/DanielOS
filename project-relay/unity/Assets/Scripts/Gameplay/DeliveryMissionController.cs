using UnityEngine;
using UnityEngine.Events;

namespace ProjectRelay.Gameplay
{
    public class DeliveryMissionController : MonoBehaviour
    {
        [Header("Mission")]
        [SerializeField] private string missionId = "M1";
        [SerializeField] private float cargoHealth = 100f;
        [SerializeField] private Transform destination;
        [SerializeField] private float successRadius = 2.5f;

        [Header("Events")]
        [SerializeField] private UnityEvent onMissionStarted;
        [SerializeField] private UnityEvent onMissionSuccess;
        [SerializeField] private UnityEvent onMissionFailed;

        private bool _started;
        private bool _finished;

        public float CargoHealth => cargoHealth;
        public bool IsStarted => _started;
        public bool IsFinished => _finished;

        public void StartMission()
        {
            if (_finished) return;
            _started = true;
            onMissionStarted?.Invoke();
            Debug.Log($"[Mission] Started: {missionId}");
        }

        public void DamageCargo(float amount)
        {
            if (!_started || _finished) return;
            cargoHealth = Mathf.Clamp(cargoHealth - amount, 0f, 100f);

            if (cargoHealth <= 0f)
                FailMission();
        }

        private void Update()
        {
            if (!_started || _finished || destination == null) return;

            if (Vector3.Distance(transform.position, destination.position) <= successRadius)
                CompleteMission();
        }

        private void CompleteMission()
        {
            _finished = true;
            onMissionSuccess?.Invoke();
            Debug.Log($"[Mission] Success: {missionId}");
        }

        private void FailMission()
        {
            _finished = true;
            onMissionFailed?.Invoke();
            Debug.Log($"[Mission] Failed: {missionId}");
        }
    }
}
